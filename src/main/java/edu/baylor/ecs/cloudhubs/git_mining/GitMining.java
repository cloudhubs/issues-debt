package edu.baylor.ecs.cloudhubs.git_mining;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.*;

public class GitMining {
    public GitMining() throws IOException {
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        String repo = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();

        BlameCommand blamer = new BlameCommand(repository);
        ObjectId commitID = repository.resolve("HEAD~~");
        blamer.setStartCommit(commitID);
        blamer.setFilePath("README.md");
        BlameResult blame = blamer.call();

        // read the number of lines from the given revision, this excludes changes from the last two commits due to the "~~" above
        int lines = countLinesOfFileInCommit(repository, commitID, "README.md");
        for (int i = 0; i < lines; i++) {
            RevCommit commit = blame.getSourceCommit(i);
            System.out.println("Line: " + i + ": " + commit);
        }

        final int currentLines;
        try (final FileInputStream input = new FileInputStream("README.md")) {
            currentLines = IOUtils.readLines(input, "UTF-8").size();
        }

        System.out.println("Displayed commits responsible for " + lines + " lines of README.md, current version has " + currentLines + " lines");
    }

    private static int countLinesOfFileInCommit(Repository repository, ObjectId commitID, String name) throws IOException {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitID);
            RevTree tree = commit.getTree();
            System.out.println("Having tree: " + tree);

            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(name));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file 'README.md'");
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // load the content of the file into a stream
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                loader.copyTo(stream);

                revWalk.dispose();

                return IOUtils.readLines(new ByteArrayInputStream(stream.toByteArray()), "UTF-8").size();
            }
        }
    }

}
