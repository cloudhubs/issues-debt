package edu.baylor.ecs.cloudhubs.git_mining;

/**
 * author: Abdullah Al Maruf
 * date: 6/25/21
 * time: 1:32 AM
 * website : https://maruftuhin.com
 */

import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.FollowFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

// Simple example that shows how to diff a single file between two commits when
// the file may have been renamed.
public class DiffRenamedFile {
    public static void main(String[] args)
            throws IOException, GitAPIException {
        String repo = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();
        ObjectId oldHead = repository.resolve("HEAD^^^^{tree}");
        ObjectId head = repository.resolve("HEAD^{tree}");
        runDiff(repository,
                "45d4c1467a8d367367889d1be6decc2007a7eddb^",
                "45d4c1467a8d367367889d1be6decc2007a7eddb",
                "README.md");

//            // try the reverse as well
//            runDiff(repository,
//                    "5a10bd6ee431e362facb03cfe763b9a3d9dfd02d",
//                    "2e1d65e4cf6c5e267e109aa20fd68ae119fa5ec9",
//                    "README.md");
//
////            // caret allows to specify "the previous commit"
//            runDiff(repository,
//                    "771fb50552bca24b0a2764e3870e05917b0bbe6e^",
//                    "771fb50552bca24b0a2764e3870e05917b0bbe6e",
//                    "README.md");
    }

    private static void runDiff(Repository repo, String oldCommit, String newCommit, String path) throws IOException, GitAPIException {
        // Diff README.md between two commits. The file is named README.md in
        // the new commit (5a10bd6e), but was named "jgit-cookbook README.md" in
        // the old commit (2e1d65e4).
        DiffEntry diff = diffFile(repo,
                oldCommit,
                newCommit,
                path);
        System.out.println(diff.getScore());

        // Display the diff
        System.out.println("Showing diff of " + path);

        try (DiffFormatter formatter = new DiffFormatter(System.out)) {
            formatter.setRepository(repo);
//            formatter.setContext(0);
            //noinspection ConstantConditions
            formatter.format(diff);
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

    private static @NonNull
    DiffEntry diffFile(Repository repo, String oldCommit,
                       String newCommit, String path) throws IOException, GitAPIException {
        Config config = new Config();
        config.setBoolean("diff", null, "renames", true);
        DiffConfig diffConfig = config.get(DiffConfig.KEY);
        try (Git git = new Git(repo)) {
            List<DiffEntry> diffList = git.diff().
                    setOldTree(prepareTreeParser(repo, oldCommit)).
                    setNewTree(prepareTreeParser(repo, newCommit)).
                    setPathFilter(FollowFilter.create(path, diffConfig)).call();
            if (diffList.size() == 0)
                return null;
            if (diffList.size() > 1)
                throw new RuntimeException("invalid diff");
            for (DiffEntry entry : diffList) {
                System.out.println(entry.getTreeFilterMarks());
            }
            return diffList.get(0);
        }
    }
}
