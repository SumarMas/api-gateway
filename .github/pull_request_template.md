# Name of Feature/Fix/Refactor
<!--- Put a name to this change -->

## Description
<!--- Describe your changes in detail -->

## Related Issue
<!--- This project only accepts pull requests related to open issues -->
<!--- If suggesting a new feature or change, please discuss it in an issue first -->
<!--- If fixing a bug, there should be an issue describing it with steps to reproduce -->
<!--- Please link to the issue here: -->

## Motivation and Context
<!--- Why is this change required? What problem does it solve? -->
<!--- If it fixes an open issue, please link to the issue here. -->

## How Has This Been Tested?
<!--- Please describe in detail how you tested your changes. -->
<!--- Include details of your testing environment, and the tests you ran to -->
<!--- see how your change affects other areas of the code, etc. -->

# Checklist:

- [ ] My code follows the style guidelines of this project
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] The test coverage is greater than 80%
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published in downstream modules
- [ ] I run "mvn javadoc:javadoc" to generate the javadoc documentation

# 🔗 Merge Convention

| Branch from  | Branch to        | Merge commit | Squash and Merge |
|--------------|------------------|--------------|------------------|
| feature      | develop          | ❌           | ✅               |
| hotfix/      | master           | ❌           | ✅               |
| fix/         | release/         | ❌           | ✅               |
| release/     | master           | ✅           | ❌               |
| backport/    | develop - release| ✅           | ❌               |

---

## ❓ What happens if I do a *merge commit* when I should have done a *squash*?

- Nothing serious, we would just be bringing unnecessary commits into the branch where we are merging.  
  Example: if in my `feature/` branch I have 15 commits and the comments are not descriptive (fix, fix tests, working), I would be pushing all of those into `develop`, while with *Squash* I would only push a single one.

---

## ❓ What happens if I do a *squash* when I should have done a *merge commit*?

- In this case, we wouldn’t be fulfilling the purpose we expect, which is to bring the commits from one branch to another in order to align them.  
- For example, when we merge a `release` into `master`, the goal is to keep `develop` and `master` aligned.  
- If we use *squash* as a merge strategy, they will continue having different commits, which could cause conflicts in the next `release`.
