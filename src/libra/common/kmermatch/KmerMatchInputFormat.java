/*
 * Copyright 2016 iychoi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libra.common.kmermatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import libra.common.hadoop.io.datatypes.CompressedSequenceWritable;
import libra.preprocess.common.filetable.FileTable;
import libra.preprocess.common.helpers.KmerIndexHelper;
import libra.preprocess.common.kmerindex.KmerIndexTable;
import libra.preprocess.common.kmerindex.KmerIndexTablePathFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.security.TokenCache;

/**
 *
 * @author iychoi
 */
public class KmerMatchInputFormat extends SequenceFileInputFormat<CompressedSequenceWritable, KmerMatchResult> {

    private static final Log LOG = LogFactory.getLog(KmerMatchInputFormat.class);

    private final static String NUM_INPUT_FILES = "mapreduce.input.num.files";
    
    @Override
    public RecordReader<CompressedSequenceWritable, KmerMatchResult> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
        return new KmerMatchRecordReader();
    }
    
    public static void setInputFormatConfig(JobContext job, KmerMatchInputFormatConfig inputFormatConfig) throws IOException {
        inputFormatConfig.saveTo(job.getConfiguration());
    }
    
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        Configuration conf = job.getConfiguration();
        KmerMatchInputFormatConfig inputFormatConfig = KmerMatchInputFormatConfig.createInstance(conf);
        
        // generate splits
        List<InputSplit> splits = new ArrayList<InputSplit>();
        List<FileStatus> files = listStatus(job);
        List<Path> kmerIndexTableFiles = new ArrayList<Path>();
        for (FileStatus file : files) {
            Path path = file.getPath();
            kmerIndexTableFiles.add(path);
        }
        
        LOG.info("# of Split input file : " + kmerIndexTableFiles.size());
        for(int i=0;i<kmerIndexTableFiles.size();i++) {
            LOG.info("> " + kmerIndexTableFiles.get(i).toString());
        }
        
        int partitions = 0;
        for(Path kmerIndexTableFile : kmerIndexTableFiles) {
            FileSystem fs = kmerIndexTableFile.getFileSystem(conf);
            KmerIndexTable indexTable = KmerIndexTable.createInstance(fs, kmerIndexTableFile);
            if(partitions == 0) {
                partitions = indexTable.getSize();
            } else {
                if(partitions != indexTable.getSize()) {
                    throw new IOException(String.format("# of partitions are different between input files - %d expected, but %d got", partitions, indexTable.getSize()));
                }
            }
        }
        
        if(partitions == 0) {
            throw new IOException("There is no partition");
        }
        
        for(int i=0;i<partitions;i++) {
            splits.add(new KmerMatchInputSplit(inputFormatConfig.getKmerSize(), kmerIndexTableFiles.toArray(new Path[0]), i));
        }
        
        // Save the number of input files in the job-conf
        job.getConfiguration().setLong(NUM_INPUT_FILES, files.size());

        LOG.debug("Total # of splits: " + splits.size());
        return splits;
    }
    
    @Override
    protected List<FileStatus> listStatus(JobContext job) throws IOException {
        Configuration conf = job.getConfiguration();
        KmerMatchInputFormatConfig inputFormatConfig = KmerMatchInputFormatConfig.createInstance(conf);
        
        List<FileStatus> result = new ArrayList<FileStatus>();
        Path[] fileTablePaths = getInputPaths(job);
        if (fileTablePaths.length == 0) {
            throw new IOException("No input paths specified in job");
        }
        
        // get tokens for all the required FileSystems..
        TokenCache.obtainTokensForNamenodes(job.getCredentials(), fileTablePaths, conf);

        List<FileTable> fileTables = new ArrayList<FileTable>();
        List<Path> inputKmerIndexTableFiles = new ArrayList<Path>();
        for(Path fileTablePath : fileTablePaths) {
            FileSystem fs = fileTablePath.getFileSystem(conf);
            FileTable ft = FileTable.createInstance(fs, fileTablePath);
            fileTables.add(ft);
            
            String kmerIndexTableFileName = KmerIndexHelper.makeKmerIndexTableFileName(ft.getName());
            Path kmerIndexTableFilePath = new Path(inputFormatConfig.getKmerIndexPath(), kmerIndexTableFileName);
            inputKmerIndexTableFiles.add(kmerIndexTableFilePath);
        }
        
        // get tokens for all the required FileSystems..
        TokenCache.obtainTokensForNamenodes(job.getCredentials(), inputKmerIndexTableFiles.toArray(new Path[0]), conf);
        
        // creates a MultiPathFilter with the hiddenFileFilter and the
        // user provided one (if any).
        List<PathFilter> filters = new ArrayList<PathFilter>();
        PathFilter jobFilter = getInputPathFilter(job);
        if (jobFilter != null) {
            filters.add(jobFilter);
        }
        filters.add(new KmerIndexTablePathFilter());
        PathFilter inputFilter = new MultiPathFilter(filters);
        
        for (int i = 0; i < inputKmerIndexTableFiles.size(); ++i) {
            Path inputKmerIndexTableFile = inputKmerIndexTableFiles.get(i);
            if(inputFilter.accept(inputKmerIndexTableFile)) {
                FileSystem fs = inputKmerIndexTableFile.getFileSystem(conf);
                FileStatus status = fs.getFileStatus(inputKmerIndexTableFile);
                result.add(status);
            }
        }

        LOG.info("Total input paths to process : " + result.size());
        return result;
    }
    
    private static class MultiPathFilter implements PathFilter {

        private List<PathFilter> filters;

        public MultiPathFilter(List<PathFilter> filters) {
            this.filters = filters;
        }

        public boolean accept(Path path) {
            for (PathFilter filter : filters) {
                if (!filter.accept(path)) {
                    return false;
                }
            }
            return true;
        }
    }
}
