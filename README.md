# rpsl4j
Routing Policy Specification Language implementation for Java

# Deploying
  1. Make sure you have the signing key (0x036FA654) in your GPG keyring.
  2. Configure your user account details for OSSRH (`~/.m2/settings.xml`)
    ```
    <settings>
      <servers>
        <server>
          <id>ossrh</id>
          <username>your-jira-id</username>
          <password>your-jira-pwd</password>
        </server>
      </servers>
    </settings>
    ```
  3. Update the version number in `pom.xml`, commit and tag (`git tag v1.80`)
  4. Deploy to the staging repository (`mvn clean deploy`)
  5. Check the staging repository [OSSRH](https://oss.sonatype.org/)
    + To deploy to the central repository: `mvn nexus-staging:release`
    + To drop the staged release: `mvn nexus-staging:drop`

__note:__ versions ending in -SNAPSHOT will be uploaded the the snapshot repository on deploy. These can be checked at [OSSRH](https://oss.sonatype.org/) and cannot be promoted to release or dropped.
