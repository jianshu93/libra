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
package libra.preprocess.common.kmerindex;

import java.io.Closeable;
import java.io.IOException;
import libra.common.hadoop.io.datatypes.CompressedIntArrayWritable;
import libra.common.hadoop.io.datatypes.CompressedSequenceWritable;
import org.apache.hadoop.fs.Path;

/**
 *
 * @author iychoi
 */
public abstract class AKmerIndexReader implements Closeable {
    @Override
    public abstract void close() throws IOException;
    public abstract Path getKmerIndexTablePath();
    public abstract boolean next(CompressedSequenceWritable key, CompressedIntArrayWritable val) throws IOException;
}
