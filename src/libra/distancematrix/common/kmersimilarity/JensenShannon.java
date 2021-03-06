/*
 * Copyright 2018 iychoi.
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
package libra.distancematrix.common.kmersimilarity;

import java.io.IOException;
import libra.distancematrix.common.WeightAlgorithm;
import libra.preprocess.common.kmerstatistics.KmerStatistics;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author iychoi
 */
public class JensenShannon extends AbstractScore {
    private static final Log LOG = LogFactory.getLog(JensenShannon.class);
    private static final double log2 = Math.log(2);
    
    private double[] param_array;
    
    public JensenShannon() {
    }
    
    @Override
    public void setParam(int size, double[] param_array) {
        if(this.param_array == null) {
            this.param_array = new double[size];
        }
        
        for(int i=0;i<size;i++) {
            this.param_array[i] = param_array[i];
        }
    }
    
    @Override
    public void setParam(int size, WeightAlgorithm algorithm, KmerStatistics[] statistics) throws IOException {
        if(this.param_array == null) {
            this.param_array = new double[size];
        }
        
        switch(algorithm) {
            case LOGARITHM:
                for(int i=0;i<size;i++) {
                    this.param_array[i] = statistics[i].getLogTFSum();
                }
                break;
            case NATURAL:
                for(int i=0;i<size;i++) {
                    this.param_array[i] = statistics[i].getNaturalTFSum();
                }
                break;
            case BOOLEAN:
                for(int i=0;i<size;i++) {
                    this.param_array[i] = statistics[i].getBooleanTFSum();
                }
                break;
            default:
                LOG.info("Unknown algorithm specified : " + algorithm.toString());
                throw new IOException("Unknown algorithm specified : " + algorithm.toString());
        }
    }
    
    @Override
    public void contributeScore(int size, double[] score_matrix, double[] score_array) {
        double[] score_array_new = new double[size];
        for(int i=0;i<size;i++) {
            score_array_new[i] = score_array[i] / this.param_array[i];
        }
        
        for(int i=0;i<size;i++) {
            for(int j=0;j<size;j++) {
                double avg = (score_array_new[i] + score_array_new[j]) / 2;
                double p1 = 0;
                double p2 = 0;
                
                if(avg == 0) {
                    continue;
                }
                
                if(score_array_new[i] != 0) {
                    p1 = score_array_new[i] * Math.log(score_array_new[i] / avg);
                }
                
                if(score_array_new[j] != 0) {
                    p2 = score_array_new[j] * Math.log(score_array_new[j] / avg);
                }

                score_matrix[i*size + j] += ((p1 + p2) / log2)/2;
            }
        }
    }
    
    @Override
    public double accumulateScore(double score1, double score2) {
        return score1 + score2;
    }
    
    @Override
    public double finalizeScore(double score) {
        // compute similarity from dissimilarity
        double similarity = 1 - score;
        if(similarity < 0) {
            similarity = 0;
        }
        
        if(similarity > 1) {
            similarity = 1;
        }
        
        return similarity;
    }
}
