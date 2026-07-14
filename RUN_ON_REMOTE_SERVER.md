# Running `Experiment.java` on a Remote Server via SSH — Zero to Hero

This guide walks through connecting to a fresh remote server over SSH, shipping this
project to it, and running `src/main/Experiment.java` there. Two paths are covered:

- **Path A — Docker (recommended)**: uses the prebuilt `scl-star.jar` + the image parts
  already in this repo (`part_aa` … `part_aj`), matching the official `README.md` workflow.
- **Path B — Native Java**: compiles `Experiment.java` directly with `javac`/`java` on the
  remote host, for when Docker isn't available or you want to iterate on the source.

Replace `user@remote-host` with your actual SSH target throughout.

---

## 0. Connect to the server

```bash
ssh user@remote-host
```

If you use a key pair instead of a password:

```bash
ssh -i /path/to/key.pem user@remote-host
```

Keep this shell open for the "on the remote" steps below; open a **second local terminal**
for the `scp`/`rsync` transfer steps.

---

## 0.5 (Re)build `scl-star.tar` from the `Dockerfile`

The `part_aa` … `part_aj` files checked into this repo are just a split copy of a
`docker save` of the `scl-star` image. If you've changed `Dockerfile`, `scl-star.jar`,
or anything else the image bundles, rebuild the image and regenerate those parts
**before** following the "Copy the project" / "Path A" steps below — otherwise the
remote server will load a stale image.

Run this on your **local machine** (project root), wherever Docker is available:

```bash
cd /home/aryan/Desktop/escl/ESCL-Star

# 1. Build the image from the Dockerfile
docker build -t scl-star .

# 2. Save it to a single tarball
docker save -o scl-star.tar scl-star

# 3. Remove the old split parts and re-split into fresh 50MB chunks
rm -f part_a*
split -b 50M -a 2 scl-star.tar part_

# 4. Clean up the intermediate tarball (the split parts are what gets committed)
rm scl-star.tar
```

This produces the same `part_aa`, `part_ab`, … naming the rest of this guide and the
`README.md` expect. Commit the regenerated `part_a*` files so the remote server (and
anyone else pulling the repo) gets the updated image.

---

## 1. Copy the project to the remote server

From your **local machine** (project root), copy the whole repo over SSH:

```bash
rsync -avz --progress \
  --exclude '.git' \
  --exclude '.idea' \
  /home/aryan/Desktop/ESCL-Star/ \
  user@remote-host:~/ESCL-Star/
```

(`rsync` resumes/retries better than `scp` for the large `part_a*` files — the image is
split into ~50MB chunks totalling several GB.) If `rsync` isn't available, use `scp -r`
instead:

```bash
scp -r /home/aryan/Desktop/ESCL-Star user@remote-host:~/ESCL-Star
```

---

## Path A — Docker (recommended, matches README)

### A.1 Install Docker on the remote server

Back in your **SSH session**:

```bash
sudo apt-get update
sudo apt-get install -y docker.io
docker --version
```

Add your user to the `docker` group so you don't need `sudo` for every command
(log out/in, or run `newgrp docker`, for this to take effect):

```bash
sudo usermod -aG docker $USER
newgrp docker
```

### A.2 Reassemble and load the Docker image

```bash
cd ~/ESCL-Star
cat part_* > scl-star.tar
docker load -i scl-star.tar
```

If you hit a permission error (`Got permission denied while trying to connect to the
Docker daemon socket`), prefix the commands with `sudo`.

### A.3 Run the experiment

```bash
cd ~/ESCL-Star
docker run -it -v "$(pwd)":/app scl-star
```

This launches the container's `CMD`, which runs:

```bash
java -cp "./libs/learnlib-distribution-0.16.0-dependencies-bundle.jar:./libs/opencsv-5.6.jar:./libs/slf4j-jdk14-1.7.36.jar:./libs/commons-cli-1.4.jar:scl-star.jar" main/Experiment
```

...and drops you into the interactive prompts defined in `Experiment.java`:

```
Choose Equivalence Query (rndWords recommended): [wp, w, wrnd, rndWords, rndWordsBig, rndWalk]
Enable Final Check Mode (disabled recommended): [true/false]
Enter Number of Repetitions (3 recommended):
Enter Test Type [Real, P2P, Ring, Star, Bus, Bipartite, Mesh]:
Enter Minimum Number of States (100 recommended):
Enter Maximum Number of States (30000 recommended):
Enter Number of Tests for each component-number:
Enter Minimum Number of Components (3 recommended):
Enter Maximum Number of Components (7 recommended for Bipartite and Mesh and 9 for others):
```

Recommended quick-run answers: `rndWords`, `false`, `3`, `Star`, `100`, `1000`, `10`, `3`, `7`.

### A.4 Run it non-interactively (optional)

To avoid retyping answers each time (useful for scripted remote runs), pipe them in:

```bash
printf 'rndWords\nfalse\n3\nStar\n100\n1000\n10\n3\n7\n' | \
  docker run -i -v "$(pwd)":/app scl-star
```

### A.5 Detach / run in background over SSH (optional)

To keep the run alive after you disconnect, use `tmux` or `screen`:

```bash
sudo apt-get install -y tmux
tmux new -s scl-star
cd ~/ESCL-Star
docker run -it -v "$(pwd)":/app scl-star
# Detach: Ctrl-b then d
# Reattach later:
tmux attach -t scl-star
```

### A.6 Exit / cleanup

Inside the container's shell prompt:

```bash
exit
```

Results land in `Results/Parameters/...` on the mounted volume, i.e. back on the remote
host at `~/ESCL-Star/Results/`.

### A.7 Pull results back to your local machine

From your **local machine**:

```bash
rsync -avz user@remote-host:~/ESCL-Star/Results/ /home/aryan/Desktop/ESCL-Star/Results/
```

---

## Path B — Native Java (no Docker)

Use this if the remote server can't run Docker, or you're actively editing
`Experiment.java` and want a faster edit/run loop.

### B.1 Install a JDK and Python 3 on the remote server

```bash
sudo apt-get update
sudo apt-get install -y openjdk-21-jdk python3
java -version   # expect 21+
python3 --version
```

The generated/real test scripts are invoked via `runFile("python", ...)` in
`Experiment.java`, so make sure a `python` binary resolves too:

```bash
sudo ln -s /usr/bin/python3 /usr/bin/python
```

### B.2 Compile the project

```bash
cd ~/ESCL-Star
mkdir -p out
javac -cp "libs/learnlib-distribution-0.16.0-dependencies-bundle.jar:libs/opencsv-5.6.jar:libs/slf4j-jdk14-1.7.36.jar:libs/commons-cli-1.4.jar" \
  -d out \
  $(find src/main -name '*.java')
```

### B.3 Run it

```bash
cd ~/ESCL-Star
java -cp "out:libs/learnlib-distribution-0.16.0-dependencies-bundle.jar:libs/opencsv-5.6.jar:libs/slf4j-jdk14-1.7.36.jar:libs/commons-cli-1.4.jar" \
  main.Experiment
```

Same interactive prompts as Path A apply. For a non-interactive/background run, use the
same `printf | java ...` piping or `tmux` approach shown in A.4/A.5.

### B.4 Pull results back to your local machine

```bash
rsync -avz user@remote-host:~/ESCL-Star/Results/ /home/aryan/Desktop/ESCL-Star/Results/
```

---

## Troubleshooting

- **`Got permission denied ... docker.sock`**: prefix Docker commands with `sudo`, or
  finish the `usermod -aG docker $USER` + re-login step from A.1.
- **Learning seems stuck (round number in the thousands, e.g. `Starting round 1001`)**:
  stop the run, keep the partial `Results.csv`, then follow the merge procedure in
  `README.md` (`Merging Tool/Merge Results.ipynb`) once you have a second run's results.
- **`python: command not found`**: create the `python -> python3` symlink shown in A.1/B.1
  (the Dockerfile does this automatically; native runs need it manually).
- **Connection drops mid-run**: always wrap long runs in `tmux`/`screen` (A.5) so an SSH
  disconnect doesn't kill the process.
