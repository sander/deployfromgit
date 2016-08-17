# deployfromgit

A servlet that provides `git pull` webhooks for GitHub and Bitbucket.

## Usage

1. Use [lein-ring](https://github.com/weavejester/lein-ring) to create a development server or deploy a war file. Configure the following properties:
   - `deployfromgit.target`: target directory, e.g. `/srv/git`
   - `deployfromgit.git.ssh.command`: ssh command with arguments to use, e.g. `ssh -i /etc/keys/deployment -o UserKnownHostsFile=/dev/null`
3. Manually clone the repositories you want to keep updated inside the `deployfromgit.target` directory, structured as `$host/$user/$repo/$branch`. For example, `/srv/git/github.com/sander/deployfromgit/master`.
4. Make sure the user running the servlet can update these repositories using the key you configured.
5. For each GitHub repository, add a webhook with Payload URL `http://yourhost/github` and the default settings.
6. For each Bitbucket repository, add a webhook with URL `http://yourhost/bitbucket` and the default settings.

Now any commit to the repositories you configured will trigger a `git pull` action in the matching clone directory.

## License

Copyright © 2016 [Sander Dijkhuis](https://sanderdijkhuis.nl/)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
