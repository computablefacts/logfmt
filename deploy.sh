#!/bin/bash
# expects variables to be set:
# - OSSRH_USERNAME
# - OSSRH_PASSWORD
# - GPG_KEY_NAME
# - GPG_PASSPHRASE
# expects file to exist:
# - .travis/gpg.asc

set -e

echo "Checking environment variables..."

if [ -z "$OSSRH_USERNAME" ]; then
  echo "Missing environment value: OSSRH_USERNAME" >&2
  exit 1
fi

if [ -z "$OSSRH_PASSWORD" ]; then
  echo "Missing environment value: OSSRH_PASSWORD" >&2
  exit 1
fi

if [ -z "$GPG_KEY_NAME" ]; then
  echo "Missing environment value: GPG_KEY_NAME" >&2
  exit 1
fi

if [ -z "$GPG_PASSPHRASE" ]; then
  echo "Missing environment value: GPG_PASSPHRASE" >&2
  exit 1
fi

if [ ! -f ".travis/gpg.asc" ]; then
  echo "Missing file: .travis/gpg.asc" >&2
  exit 1
fi

echo "All environment variables are set."
echo "Preparing the local keyring... (requires travis to have decrypted the file beforehand)"

gpg --fast-import .travis/gpg.asc

echo "The import of GPG keys succeeded."

if [ ! -z "$TRAVIS_TAG" ]; then
  echo "On a tag -> Set pom.xml <version> to $TRAVIS_TAG"
  mvn --settings "${TRAVIS_BUILD_DIR}/.travis/mvn-settings.xml" org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=$TRAVIS_TAG 1>/dev/null 2>/dev/null
else
  echo "Not on a tag -> Keep snapshot version in pom.xml"
fi

echo "Running the maven deploy steps..."

mvn deploy -P publish -DskipTests=true --settings "${TRAVIS_BUILD_DIR}/.travis/mvn-settings.xml"

echo "Maven deploy succeeded."
