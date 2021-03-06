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
package libra.preprocess.common.samplegroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import libra.common.json.JsonSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author iychoi
 */
public class SampleGroup {
    
    private static final Log LOG = LogFactory.getLog(SampleGroup.class);
    
    private static final String HADOOP_CONFIG_KEY = "libra.preprocess.common.samplegroup.samplegroup";
    
    private String name;
    private long totalSampleSize = 0;
    private List<SampleInfo> samples = new ArrayList<SampleInfo>();
    
    public static SampleGroup createInstance(File file) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        return (SampleGroup) serializer.fromJsonFile(file, SampleGroup.class);
    }
    
    public static SampleGroup createInstance(String json) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        return (SampleGroup) serializer.fromJson(json, SampleGroup.class);
    }
    
    public static SampleGroup createInstance(Configuration conf) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        return (SampleGroup) serializer.fromJsonConfiguration(conf, HADOOP_CONFIG_KEY, SampleGroup.class);
    }
    
    public static SampleGroup createInstance(FileSystem fs, Path file) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        return (SampleGroup) serializer.fromJsonFile(fs, file, SampleGroup.class);
    }
    
    public SampleGroup() {
    }
    
    public SampleGroup(String name) {
        this.name = name;
    }
    
    @JsonProperty("name")
    public String getName() {
        return this.name;
    }
    
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
    
    @JsonIgnore
    public int numSamples() {
        return this.samples.size();
    }
    
    @JsonIgnore
    public long totalSampleSize() {
        return this.totalSampleSize;
    }
    
    @JsonProperty("samples")
    public Collection<SampleInfo> getSamples() {
        return this.samples;
    }
    
    @JsonProperty("samples")
    public void addSamples(Collection<SampleInfo> samples) {
        for(SampleInfo sampleInfo : samples) {
            addSample(sampleInfo);
        }
    }
    
    @JsonIgnore
    public void addSample(SampleInfo sample) {
        this.samples.add(sample);
        this.totalSampleSize += sample.getSize();
    }
    
    @JsonIgnore
    public void clearSamples() {
        this.samples.clear();
        this.totalSampleSize = 0;
    }
    
    @JsonIgnore
    public void saveTo(Configuration conf) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        serializer.toJsonConfiguration(conf, HADOOP_CONFIG_KEY, this);
    }
    
    @JsonIgnore
    public void saveTo(FileSystem fs, Path file) throws IOException {
        JsonSerializer serializer = new JsonSerializer();
        serializer.toJsonFile(fs, file, this);
    }
}
