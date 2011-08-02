/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.mapred;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.mapreduce.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.authorize.AccessControlList;
import org.apache.hadoop.security.token.Token;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Collections;

public class MockJobTracker implements JobSubmissionProtocol {

  private RPC.Server server;
  public static final int PORT = 39737;
  private LocalFileSystem localFileSystem;

  public MockJobTracker(Configuration conf) throws IOException {
    localFileSystem = FileSystem.getLocal(new Configuration());
    InetSocketAddress addr = new InetSocketAddress("localhost", PORT);
    server = RPC.getServer(this, addr.getHostName(), addr.getPort(), 1,
          false, conf, null);
    server.start();
  }

  public void shutdown() {
    server.stop();
  }

  public static Job getJobForClient() throws IOException {
    Job job = new Job(new Configuration());
    job.getConfiguration().set("mapred.job.tracker", "localhost:" + PORT);
    job.setInputFormatClass(NullInputFormat.class);
    job.setOutputFormatClass(NullOutputFormat.class);
    job.setNumReduceTasks(0);
    return job;
  }

  public long getProtocolVersion(String protocol, long clientVersion) {
    return JobSubmissionProtocol.versionID;
  }

  @Override
  public JobID getNewJobId() throws IOException {
    return new JobID("1234", 1);
  }

  @Override
  public JobStatus submitJob(JobID jobName,
                             String jobSubmitDir, 
                             Credentials ts) throws IOException {
    return null;
  }

  @Override
  public ClusterStatus getClusterStatus(boolean detailed) throws IOException {
    return null;
  }

  @Override
  public AccessControlList getQueueAdmins(String queueName) throws IOException {
    return null;
  }

  @Override
  public void killJob(JobID jobid) throws IOException {
  }

  @Override
  public void setJobPriority(JobID jobid, String priority) throws IOException {
  }

  @Override
  public boolean killTask(TaskAttemptID taskId, boolean shouldFail) throws IOException {
    return false;
  }

  @Override
  public JobProfile getJobProfile(JobID jobid) throws IOException {
    return new JobProfile("usr", new JobID("!243", 1), "/tmp/file", "http://hurlurl/", "job");
  }

  @Override
  public JobStatus getJobStatus(JobID jobid) throws IOException {
    return new JobStatus(jobid, 1, 1, 1, JobStatus.SUCCEEDED);
  }

  @Override
  public org.apache.hadoop.mapred.Counters getJobCounters(JobID jobid) throws IOException {
    return null;
  }

  @Override
  public TaskReport[] getMapTaskReports(JobID jobid) throws IOException {
    return new TaskReport[0];
  }

  @Override
  public TaskReport[] getReduceTaskReports(JobID jobid) throws IOException {
    return new TaskReport[0];
  }

  @Override
  public TaskReport[] getCleanupTaskReports(JobID jobid) throws IOException {
    return new TaskReport[0];
  }

  @Override
  public TaskReport[] getSetupTaskReports(JobID jobid) throws IOException {
    return new TaskReport[0];
  }

  @Override
  public String getFilesystemName() throws IOException {
    return null;
  }

  @Override
  public JobStatus[] jobsToComplete() throws IOException {
    return new JobStatus[0];
  }

  @Override
  public JobStatus[] getAllJobs() throws IOException {
    return new JobStatus[0];
  }

  @Override
  public TaskCompletionEvent[] getTaskCompletionEvents(JobID jobid,
                                                       int fromEventId,
                                                       int maxEvents) throws IOException {
    return new TaskCompletionEvent[0];
  }

  @Override
  public String[] getTaskDiagnostics(TaskAttemptID taskId) throws IOException {
    return new String[0];
  }

  @Override
  public String getSystemDir() {
    return new Path("target/system").makeQualified(localFileSystem).toString();
  }

  @Override
  public String getStagingAreaDir() throws IOException {
    localFileSystem.mkdirs(new Path("target/staging"),
        new FsPermission((short) 0700));    
    return "target/staging";
  }

  @Override
  public JobQueueInfo[] getQueues() throws IOException {
    return new JobQueueInfo[0];
  }

  @Override
  public JobQueueInfo getQueueInfo(String queue) throws IOException {
    return null;
  }

  @Override
  public JobStatus[] getJobsFromQueue(String queue) throws IOException {
    return new JobStatus[0];
  }

  @Override
  public QueueAclsInfo[] getQueueAclsForCurrentUser() throws IOException {
    return new QueueAclsInfo[0];
  }

  @Override
  public Token<DelegationTokenIdentifier> getDelegationToken(Text renewer)
      throws IOException, InterruptedException {
    return null;
  }

  @Override
  public void cancelDelegationToken(Token<DelegationTokenIdentifier> token)
      throws IOException, InterruptedException {
  }

  @Override
  public long renewDelegationToken(Token<DelegationTokenIdentifier> token)
      throws IOException, InterruptedException {
    return 0;
  }

  private static class NullInputFormat extends InputFormat {
    @Override
    public List getSplits(JobContext context)
        throws IOException, InterruptedException {
      return Collections.EMPTY_LIST;
    }

    @Override
    public RecordReader createRecordReader(InputSplit split,
                                           TaskAttemptContext context)
        throws IOException, InterruptedException {
      return null;
    }
  }
}
