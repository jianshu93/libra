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

import libra.common.hadoop.io.datatypes.IntArrayWritable;
import libra.common.hadoop.io.datatypes.CompressedSequenceWritable;

/**
 *
 * @author iychoi
 */
public class KmerIndexRecordBufferEntry {
    private CompressedSequenceWritable key;
    private IntArrayWritable val;

    public KmerIndexRecordBufferEntry(CompressedSequenceWritable key, IntArrayWritable val) {
        this.key = key;
        this.val = val;
    }

    public CompressedSequenceWritable getKey() {
        return this.key;
    }

    public IntArrayWritable getVal() {
        return this.val;
    }
}
