package edu.baylor.ecs.cloudhubs.git_mining;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Simple snippet which shows how to retrieve the diffs
 * between two commits
 */
public class DiffFilesInCommit {

    public static void main(String[] args) throws IOException, GitAPIException {
        String repo = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();

        try (Git git = new Git(repository)) {

            // compare older commit with the newer one, showing an addition
            // and 2 changes
            listDiff(repository, git,
                    "771fb50552bca24b0a2764e3870e05917b0bbe6e^",
                    "771fb50552bca24b0a2764e3870e05917b0bbe6e");

            // also the diffing the reverse works and now shows a delete
            // instead of the added file
//            listDiff(repository, git,
//                    "19536fe5765ee79489265927a97cb0e19bb93e70",
//                    "3cc51d5cfd1dc3e890f9d6ded4698cb0d22e650e");

            // to compare against the "previous" commit, you can use
            // the caret-notation
//            listDiff(repository, git,
//                    "19536fe5765ee79489265927a97cb0e19bb93e70^",
//                    "19536fe5765ee79489265927a97cb0e19bb93e70");
        }
    }

    private static void listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
}
