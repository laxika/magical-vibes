---
name: unused-variables
description: >-
  Scan and/or remove unused Java variables in specific magical-vibes Gradle
  modules (e.g. "scan unused variables in domain", "remove unused vars from
  engine", "clean unused variables in magical-vibes-networking"). Uses opt-in
  Error Prone UnusedVariable Gradle tasks; does not run on ordinary build/test.
---

# Unused variables

Two-step workflow for **Java** modules only (`magical-vibes-frontend` is out of scope).

Error Prone `UnusedVariable` covers:
- unused **local** variables
- unused **private** fields (and effectively-private members)
- unused parameters of **private** methods

It does **not** reliably flag unused public methods/classes, Spring beans, Jackson DTOs, `@CardRegistration` cards, or effect types reached only via `instanceof` / reflection. Do not treat a clean scan as “no dead code.”

Suppress legitimate cases by renaming a parameter/field with an `unused` prefix, or `@SuppressWarnings("unused")` / `@Keep` where reflection needs the member.

## Step 1 — Scan (read-only)

Ask which module(s) if the user did not name them. Valid module names match `settings.gradle.kts` (e.g. `magical-vibes-domain`, `magical-vibes-engine`).

```bash
./gradlew :magical-vibes-domain:scanUnusedVariables --rerun-tasks
```

Multiple modules:

```bash
./gradlew :magical-vibes-domain:scanUnusedVariables :magical-vibes-networking:scanUnusedVariables --rerun-tasks
```

Always pass `--rerun-tasks` so a prior normal compile is not reused without Error Prone.

Report findings from the compiler warnings (`[UnusedVariable]`). Do **not** modify sources in this step. If the user only asked to scan, stop here.

## Step 2 — Remove (only when the user asks)

Never apply patches unless the user explicitly asks to remove/fix/clean unused variables.

```bash
./gradlew :magical-vibes-domain:removeUnusedVariables --rerun-tasks
```

Then:
1. `git diff` the touched module(s) and summarize what changed.
2. Spot-check that removals look safe (no “unused” that is actually read via reflection without `@Keep`).
3. Do **not** commit unless the user asks.

## Notes

- Ordinary `build` / `test` / `compileJava` leave Error Prone **disabled**; these tasks are opt-in only.
- After `removeUnusedVariables`, optionally re-run `scanUnusedVariables` on the same module(s) to confirm the warnings are gone.
- Do not run the full test suite; if verification is needed, ask the user or run a narrow module test they approve.
