package edu.baylor.ecs.cloudhubs.git_mining;
/*
   Copyright 2013, 2014 Dominik Stadler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Simple snippet which shows how to show diffs between branches
 *
 * @author dominik.stadler at gmx.at
 */
public class ShowFileDiff {

    public static void main(String[] args) throws IOException, GitAPIException {
        String repo = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();
        ObjectId oldHead = repository.resolve("HEAD^^^^{tree}");
        ObjectId head = repository.resolve("HEAD^{tree}");

        // the diff works on TreeIterators, we prepare two for the two branches
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, "771fb50552bca24b0a2764e3870e05917b0bbe6e");
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, "00f5c665cc69add3c85726b5d9ae698aa734dba6");

        // then the porcelain diff-command returns a list of diff entries
        try (Git git = new Git(repository)) {
            List<DiffEntry> diff = git.diff().
                    setOldTree(oldTreeParser).
                    setNewTree(newTreeParser).
                    setPathFilter(PathFilter.create("README.md")).
                    // to filter on Suffix use the following instead
                    //setPathFilter(PathSuffixFilter.create(".java")).
                            call();
            for (DiffEntry entry : diff) {
                System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                    formatter.setRepository(repository);
                    formatter.format(entry);
                }
            }
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
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