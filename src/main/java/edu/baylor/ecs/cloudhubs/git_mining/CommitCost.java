package edu.baylor.ecs.cloudhubs.git_mining;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommitCost {


    private Git git;
    private Repository repo;
    String RepoPath = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";
    String hashID = "c7caed4c8ffe90cc5b777d8e62903288e551d933";

    public static void main(String[] args) throws IOException, GitAPIException {
        CommitCost obj = new CommitCost();
        obj.doWork();
    }

    public void doWork() throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repo = builder.setGitDir(new File(RepoPath + "/.git")).setMustExist(true)
                .build();
        git = new Git(repo);
        RevCommit newCommit = toCommitHash(hashID);
        RevCommit oldCommit = getPrevHash(newCommit);
        if (oldCommit == null) {
            System.out.println("Start of repo");
            return;
        }

        String[] paths = getChangedFiles(newCommit, oldCommit);
        System.out.println(Arrays.toString(paths));
        HashMap<String, BlameResult> new_blames = getBlame(newCommit, paths);
        HashMap<String, BlameResult> old_blames = getBlame(oldCommit, paths);
        getEdits(newCommit, oldCommit, new_blames, old_blames);

//        blames.forEach((k,v) -> System.out.println("Key = "
//                + k + ", Value = " + v.getSourceCommit(0).getCommitTime()));
//
//        System.out.println("....>>>>>"+blames.get("cms/src/main/java/edu/baylor/ecs/cms/service/QmsService.java"));
    }


    private AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = walk.parseCommit(commitId);
            ObjectId treeId = commit.getTree().getId();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                return new CanonicalTreeParser(null, reader, treeId);
            }
        }
    }

    //Helper gets the diff as a string.
    private String getDiffOfCommit(RevCommit newCommit, RevCommit oldCommit) throws IOException {

        //Use treeIterator to diff.
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
        OutputStream outputStream = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
            formatter.setRepository(git.getRepository());
            formatter.format(oldTreeIterator, newTreeIterator);
        }
        String diff = outputStream.toString();
        System.out.println(diff);
        return diff;
    }

    private HashMap<String, BlameResult> getBlame(RevCommit commit, String[] paths) throws IOException, GitAPIException {
//        git.getRepository().resolve(commit.getName());
        git.checkout().setName(commit.getName()).call();


        HashMap<String, BlameResult> blames = new HashMap<>();

        if (paths == null) {
            throw new IllegalStateException("Did not find any files at " + new File(".").getAbsolutePath());
        }

        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd HH:mm");
        for (String file : paths) {
            if (new File(RepoPath, file).isDirectory()) {
                continue;
            }

            System.out.println("Blaming " + file);
            final BlameResult result = new Git(repo).blame().setFilePath(file)
                    .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();
            final RawText rawText = result.getResultContents();
            for (int i = 0; i < rawText.size(); i++) {
                final PersonIdent sourceAuthor = result.getSourceAuthor(i);
                final RevCommit sourceCommit = result.getSourceCommit(i);
//                System.out.println(i+ " "+sourceAuthor.getName() +
//                        (sourceCommit != null ? " - " + DATE_FORMAT.format(((long) sourceCommit.getCommitTime()) * 1000) +
//                                " - " + sourceCommit.getName() : "") +
//                        ": " + rawText.getString(i));
                blames.put(file, result);
            }
        }
        return blames;
    }

    private String[] getChangedFiles(RevCommit newCommit, RevCommit oldCommit) throws IOException, GitAPIException {
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);

        List<DiffEntry> diffs = git.diff()
                .setNewTree(oldTreeIterator)
                .setOldTree(newTreeIterator)
                .call();

        List<String> arrlist
                = new ArrayList<String>();

        for (DiffEntry entry : diffs) {
            arrlist.add(entry.getOldPath());
            if (!entry.getOldPath().equals(entry.getNewPath())) {
                arrlist.add(entry.getNewPath());
            }
        }
        String[] paths = new String[arrlist.size()];
        return arrlist.toArray(paths);
    }

    private void getEdits(RevCommit newCommit, RevCommit oldCommit, HashMap<String, BlameResult> new_blames, HashMap<String, BlameResult> old_blames) throws IOException, GitAPIException {
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
        int latest_commit_time = newCommit.getCommitTime();
        int total_time = 0;
        int total_affected_line = 0;

        List<DiffEntry> diffs = git.diff()
                .setNewTree(oldTreeIterator)
                .setOldTree(newTreeIterator)
                .call();

        for (DiffEntry entry : diffs) {
//            System.out.println("old: " + entry.getOldId() +
//                    ", new: " + entry.getNewPath() +
//                    ", entry: " + entry);

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repo);
            diffFormatter.setContext(0);
            FileHeader header = diffFormatter.toFileHeader(entry);
            List<? extends HunkHeader> hunks = header.getHunks();
            System.out.println(".............. Old path " + header.getOldPath());
            System.out.println(".............. New path " + header.getNewPath());

            // A means latest commit, B is older commit
            for (HunkHeader hunkHeader : hunks) {
                for (Edit edit : hunkHeader.toEditList()) {
                    System.out.println("A: " + edit.getBeginA() + " " + edit.getEndA());
                    System.out.println("B: " + edit.getBeginB() + " " + edit.getEndB());
                    long t_old = 0;
                    long t_new = 0;


                    for (int i = edit.getBeginB(); i < edit.getEndB(); i++) {
//                        System.out.println(i+" "+old_blames.get(header.getOldPath()).getResultContents().getString(i));
                        System.out.println(i + " " + old_blames.get(header.getOldPath()).getSourceCommit(i).getCommitterIdent());
                        t_old += old_blames.get(header.getOldPath()).getSourceCommit(i).getCommitTime();
                    }
                    for (int i = edit.getBeginA(); i < edit.getEndA(); i++) {
//                        System.out.println(i+" "+new_blames.get(header.getNewPath()).getResultContents().getString(i));

                        System.out.println(i + " " + new_blames.get(header.getNewPath()).getSourceCommit(i).getCommitterIdent());
                        t_new += new_blames.get(header.getNewPath()).getSourceCommit(i).getCommitTime();
                    }
                    if (t_old == 0) {
                        System.out.println("old <>" + old_blames.get(header.getOldPath()).getSourceCommit(edit.getBeginB()).getCommitterIdent());

                        t_old = old_blames.get(header.getOldPath()).getSourceCommit(edit.getBeginB()).getCommitTime();
                    } else {
                        t_old /= edit.getEndB() - edit.getBeginB();
                    }
                    if (t_new == 0) {
                        System.out.println("new <>" + new_blames.get(header.getOldPath()).getSourceCommit(edit.getBeginA()).getCommitterIdent());

                        // if no line is added, use timeline of latest commit for diff.
                        t_new = latest_commit_time;
                    } else {
                        t_new /= edit.getEndA() - edit.getBeginA();
                    }



                    int affected_lines = edit.getEndB() - edit.getBeginB() + edit.getEndA() - edit.getBeginA();
                    total_affected_line += affected_lines;
                    System.out.println("average time: " + ((t_new - t_old) * affected_lines) / (60 * 60 * 24));

                    total_time += (t_new - t_old) * affected_lines;
                    System.out.println("total time: " + total_time/ (60 * 60 * 24));
                    System.out.println("total lines: " + total_affected_line);
                    System.out.println("Affected lines: " + affected_lines);
                }
            }

        }
        System.out.println("-----------------------------------------------");
        System.out.println("Total time: " + total_time/ (60 * 60 * 24));
        System.out.println("Total affected line: " + total_affected_line);
        System.out.println("Total average time: " + (total_time / total_affected_line) / (60 * 60 * 24));
    }

    public RevCommit toCommitHash(String hashID) throws IOException {
        RevCommit newCommit;
        try (RevWalk walk = new RevWalk(repo)) {
            newCommit = walk.parseCommit(repo.resolve(hashID));
        }
        System.out.println("LogCommit: " + newCommit);
        String logMessage = newCommit.getFullMessage();
        System.out.println("LogMessage: " + logMessage);
        //Print diff of the commit with the previous one.
        return newCommit;
    }

    //Helper function to get the previous commit.
    public RevCommit getPrevHash(RevCommit commit) throws IOException {

        try (RevWalk walk = new RevWalk(repo)) {
            // Starting point
            walk.markStart(commit);
            int count = 0;
            for (RevCommit rev : walk) {
                // got the previous commit.
                if (count == 1) {
                    return rev;
                }
                count++;
            }
            walk.dispose();
        }
        return null;
    }

}