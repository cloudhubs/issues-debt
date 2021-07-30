package edu.baylor.ecs.cloudhubs.git_mining;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChangedFilesBetweenCommit2 {


    private Git git;
    private Repository repo;
    String RepoPath = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";

    public static void main(String[] args) throws IOException, GitAPIException {
        ChangedFilesBetweenCommit2 obj = new ChangedFilesBetweenCommit2();
        obj.diffCommit("c7caed4c8ffe90cc5b777d8e62903288e551d933");
//        obj.commit_logs();

    }

    public void commit_logs() throws IOException, NoHeadException, GitAPIException {
        List<String> logMessages = new ArrayList<String>();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(new File(RepoPath + "/.git"))
                .setMustExist(true).build();
        git = new Git(repo);
        Iterable<RevCommit> log = git.log().call();
        RevCommit previousCommit = null;
        for (RevCommit commit : log) {
            if (previousCommit != null) {
                AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser( previousCommit );
                AbstractTreeIterator newTreeIterator = getCanonicalTreeParser( commit );
                OutputStream outputStream = new ByteArrayOutputStream();
                try( DiffFormatter formatter = new DiffFormatter( outputStream ) ) {
                    formatter.setRepository( git.getRepository() );
                    formatter.format( oldTreeIterator, newTreeIterator );
                }
                String diff = outputStream.toString();
                System.out.println(diff);
            }
            System.out.println("LogCommit: " + commit);
            String logMessage = commit.getFullMessage();
            System.out.println("LogMessage: " + logMessage);
            logMessages.add(logMessage.trim());
            previousCommit = commit;
        }
        git.close();
    }


    private AbstractTreeIterator getCanonicalTreeParser( ObjectId commitId ) throws IOException {
        try( RevWalk walk = new RevWalk( git.getRepository() ) ) {
            RevCommit commit = walk.parseCommit( commitId );
            ObjectId treeId = commit.getTree().getId();
            try( ObjectReader reader = git.getRepository().newObjectReader() ) {
                return new CanonicalTreeParser( null, reader, treeId );
            }
        }
    }

    public void diffCommit(String hashID) throws IOException {
        //Initialize repositories.
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repo = builder.setGitDir(new File(RepoPath + "/.git")).setMustExist(true)
                .build();
        git = new Git(repo);

        //Get the commit you are looking for.
        RevCommit newCommit;
        try (RevWalk walk = new RevWalk(repo)) {
            newCommit = walk.parseCommit(repo.resolve(hashID));
        }

        System.out.println("LogCommit: " + newCommit);
        String logMessage = newCommit.getFullMessage();
        System.out.println("LogMessage: " + logMessage);
        //Print diff of the commit with the previous one.
        System.out.println(getDiffOfCommit(newCommit));

    }
    //Helper gets the diff as a string.
    private String getDiffOfCommit(RevCommit newCommit) throws IOException {

        //Get commit that is previous to the current one.
        RevCommit oldCommit = getPrevHash(newCommit);
        if(oldCommit == null){
            return "Start of repo";
        }
        //Use treeIterator to diff.
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
        OutputStream outputStream = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
            formatter.setRepository(git.getRepository());
            formatter.format(oldTreeIterator, newTreeIterator);
        }
        String diff = outputStream.toString();
        return diff;
    }
    //Helper function to get the previous commit.
    public RevCommit getPrevHash(RevCommit commit)  throws  IOException {

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
        //Reached end and no previous commits.
        return null;
    }

}