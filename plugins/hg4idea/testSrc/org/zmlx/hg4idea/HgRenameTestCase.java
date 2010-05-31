// Copyright 2008-2010 Victor Iacoban
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under
// the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// either express or implied. See the License for the specific language governing permissions and
// limitations under the License.
package org.zmlx.hg4idea;

import com.intellij.openapi.vfs.*;
import org.testng.annotations.*;

import java.io.*;

public class HgRenameTestCase extends HgTestCase {

  @Test
  public void testRenameUnmodifiedFile() throws Exception {
    VirtualFile file = createFileInCommand("a.txt", "new file content");
    runHgOnProjectRepo("commit", "-m", "added file");
    renameFileInCommand(file, "b.txt");
    verify(runHgOnProjectRepo("status"), added("b.txt"), removed("a.txt"));
  }

  @Test
  public void testRenameModifiedFile() throws Exception {
    VirtualFile file = createFileInCommand("a.txt", "new file content");
    runHgOnProjectRepo("commit", "-m", "added file");
    editFileInCommand(myProject, file, "modified new file content");
    verify(runHgOnProjectRepo("status"), modified("a.txt"));
    renameFileInCommand(file, "b.txt");
    verify(runHgOnProjectRepo("status"), added("b.txt"), removed("a.txt"));
  }

  @Test
  public void testRenameNewFile() throws Exception {
    VirtualFile file = createFileInCommand("a.txt", "new file content");
    renameFileInCommand(file, "b.txt");
    verify(runHgOnProjectRepo("status"), added("b.txt"));
  }

  @Test
  public void testRenameRenamedFile() throws Exception {
    VirtualFile file = createFileInCommand("a.txt", "new file content");
    runHgOnProjectRepo("commit", "-m", "added file");
    renameFileInCommand(file, "b.txt");
    renameFileInCommand(file, "c.txt");
    verify(runHgOnProjectRepo("status"), added("c.txt"), removed("a.txt"));
  }

  @Test
  public void testRenameVersionedFolder() throws Exception {
    VirtualFile parent = createDirInCommand(myWorkingCopyDir, "com");
    createFileInCommand(parent, "a.txt", "new file content");
    runHgOnProjectRepo("commit", "-m", "added file");
    renameFileInCommand(parent, "org");
    verify(runHgOnProjectRepo("status"), added("org", "a.txt"), removed("com", "a.txt"));
  }

  @Test
  public void testRenameUnversionedFolder() throws Exception {
    VirtualFile parent = createDirInCommand(myWorkingCopyDir, "com");

    File unversionedFile = new File(parent.getPath(), "a.txt");
    makeFile(unversionedFile);
    verify(runHgOnProjectRepo("status"), unknown("com", "a.txt"));

    renameFileInCommand(parent, "org");
    verify(runHgOnProjectRepo("status"), unknown("org", "a.txt"));
  }

  @Test
  public void testRenameUnversionedFile() throws Exception {
    File unversionedFile = new File(myWorkingCopyDir.getPath(), "a.txt");
    VirtualFile file = makeFile(unversionedFile);
    verify(runHgOnProjectRepo("status"), unknown("a.txt"));

    renameFileInCommand(file, "b.txt");
    verify(runHgOnProjectRepo("status"), unknown("b.txt"));
  }

}
