package edu.baylor.ecs.cloudhubs.commit_logs;

import kotlin.text.Regex;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main( String[] args ) throws IOException, GitAPIException {
        String repo = "/Users/noah/desktop/CommitLogs/ccx-notification-writer";
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();


        String repoName = repository.toString();
        repoName = repoName.replaceAll("\\[", "").replaceAll("\\]","");
        //System.out.println(repoName);
        repoName = repoName.substring(nthLastIndexOf(3, "/", repoName)+1,nthLastIndexOf(1,"/",repoName)); // substring up to 2nd last included

        File commitFile = new File(repoName);
        commitFile.mkdirs();
        File commitLogs = new File(commitFile, "commitLogs.csv");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(commitLogs)));
        out.writeChars("Commit, Title, Description, Commit Time, Author" + "\n");


        try (Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log().all().call();
            for (RevCommit commit : commits) {
                writeCommit(commit, out);
            }
            out.close();
        }
    }

    private static void writeCommit(RevCommit commit, DataOutputStream out) throws IOException {

        PersonIdent person = commit.getAuthorIdent();
        out.write(commit.toString().getBytes(StandardCharsets.UTF_8));
        out.write(",".getBytes(StandardCharsets.UTF_8));

        String commitName = commit.getFullMessage().replaceAll(",","");
        String[] split = commitName.split("[\\t\\n\\r]+");

        //The title
        out.write(split[0].getBytes(StandardCharsets.UTF_8));
        out.write(",".getBytes(StandardCharsets.UTF_8));
        //The description
        for(int i = 1; i < split.length; i++){
            out.write(split[i].getBytes(StandardCharsets.UTF_8));
            out.write(" ".getBytes(StandardCharsets.UTF_8));
        }
        out.write(",".getBytes(StandardCharsets.UTF_8));


        out.write(person.getWhen().toString().getBytes(StandardCharsets.UTF_8));

        out.write(",".getBytes(StandardCharsets.UTF_8));

        out.write(person.getName().getBytes(StandardCharsets.UTF_8));
        out.writeChars("\n");
    }

    static int nthLastIndexOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }
}
