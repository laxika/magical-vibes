---
name: review-card
description: Review one or more already-implemented Magic cards in the magical-vibes engine from a set code and collector number(s) (e.g. "review DKA 76", "review SOS 1 2 3"). Checks oracle fidelity, effect reuse, targeting, and tests. May add tests for coverage gaps or edge cases. Writes findings only when issues exist.
---

# Review a card

Input is a **set code + one or more collector numbers** from that set (e.g. `DKA 76`, or `SOS 1 2 3 4`). If the user gives a card name instead, look up the set/collector number first.

**Multiple collector numbers:** review each card independently, one at a time — finish Steps 1–6 fully for one collector number before moving to the next. Keep each review's context small.

This is a **read-only review of implementation**. Do **not** edit card classes, effects, predicates, or docs unless the user explicitly asks you to fix findings afterward. Do not commit.

**Tests are the exception:** you **should** add new focused tests when coverage is incomplete or you spot plausible edge cases (see Step 4). Do not rewrite or delete existing tests unless they are clearly wrong (wrong oracle expectation); prefer adding cases. New tests follow CLAUDE.md conventions (harness/`gs`/`gd` behavior only — no Scryfall metadata or white-box wiring assertions).

The hard rules in `CLAUDE.md` (rules accuracy, reuse over creation, testing conventions) are the review criteria.

## Step 1 — Gather context

Run the helper once (same as implement-card). It fetches Scryfall oracle text, finds the existing class/test paths, and prints reprint status:

```
bash -c 'powershell.exe -NoProfile -File scripts/implement-card-context.ps1 <SET> <COLLECTOR_NUMBER> [<COLLECTOR_NUMBER> ...]'
```

If the script reports **EXISTING CLASS FOUND** for a reprint registration only (no unique logic in this set's class), say so briefly and treat a correct `@CardRegistration` as a pass unless the shared class itself is wrong for this printing's oracle.

If no class exists for this set/collector number, that is a finding (card not implemented).

## Step 2 — Read the implementation and tests

Read the card class and its test (if any). Do not assert or re-check Scryfall-loaded metadata (name, type, mana, color, P/T, subtypes, keywords) — those are auto-loaded.

## Step 3 — Check against oracle and engine conventions

Using oracle text from Step 1, verify:

1. **Oracle fidelity / rules accuracy** — every ability and mode is implemented; no invented behavior; ambiguous text checked against official rulings when needed.
2. **Reuse over creation** — effects/predicates composed from existing building blocks; no unnecessary new effect/predicate classes.
3. **Slots, targeting, costs** — correct `EffectSlot`s, target filters/`TargetSpec`, triggered/activated ability wiring. Grep `agent-docs/` when unsure (never read those docs in full): `CARD_PATTERN_INDEX.md`, `ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_QUICK_REFERENCE.md`, `TRIGGER_SLOT_TARGETING.md`, `ACTIVATED_ABILITY_GUIDE.md`, `PREDICATES_REFERENCE.md`.
4. **Constructor hygiene** — constructor is engine logic only; no manual metadata setters.
5. **Tests** — present when required (not basic land / vanilla); exercise behavior through the harness (`harness`/`gs`/`gd`), not Scryfall metadata or white-box wiring (`getEffects`, `EffectSlot`, reflection). Skip running the full suite; optionally run the focused test via `scripts/run-card-test.ps1` only if needed to confirm a suspected bug.

## Step 4 — Improve test coverage (encouraged)

After the review above, **add tests** when any of the following apply. Prefer adding over skipping — new cases often surface odd engine/oracle mismatches.

Add tests when:
- The card has abilities/modes/choices that existing tests never exercise (coverage below full oracle surface).
- You can invent a realistic edge case (timing, multiple targets, empty zones, replacement interactions, “may” declines, zero/X values, opposing controllers, stack interactions, etc.) that is not covered.
- A finding is subtle or intermittent — a failing (or narrowly asserting) test documents it for later fix.

Do **not** add tests for:
- Basic lands / true vanillas with nothing to assert beyond metadata.
- Scryfall-loaded fields or white-box wiring (`getEffects`, `EffectSlot`, reflection).
- Pure style/convention nits with no behavioral stake.

If you add tests:
- Put them in the card’s existing test class (create the test file only if missing and tests are required).
- Match nearby `BaseCardTest` patterns; keep cases focused and named for the behavior.
- Optionally run only that card’s tests via `scripts/run-card-test.ps1`. If a new test fails and confirms a real bug, that is a **FAIL** finding (implementation still wrong) — leave the failing test in place unless it is flaky/invalid; note it in the result file. If the new test passes, that strengthens coverage; it alone does not create a FAIL.

Still do **not** change production card/effect code in this step.

## Step 5 — Verdict and result file

Normalize set code to uppercase for the path. Collector number is the numeric id as given (e.g. `76`).

Result path:

```
scripts/result/<SET>/<COLLECTOR_NUMBER>.txt
```

- **Pass (no errors):** do **not** create or keep a result file. If a stale `scripts/result/<SET>/<COLLECTOR_NUMBER>.txt` exists from a previous review, **delete it**. Reply with a one-line pass summary only (mention briefly if you added tests).
- **Fail (any issue):** create `scripts/result/<SET>/` if needed and write `scripts/result/<SET>/<COLLECTOR_NUMBER>.txt` with a concise report:
  - card name, set, collector number
  - class / test paths
  - bullet list of issues (what is wrong, why it matters vs oracle/rules/conventions)
  - optional short suggested fix direction (no production code edits)
  - note any tests you added (and whether they pass/fail)

Severity: only real correctness / convention problems. Do not nitpick style, comments, or naming unless they cause wrong behavior or violate CLAUDE.md testing/implementation rules. Missing or thin behavioral coverage is worth fixing by **adding tests** (Step 4), not by failing the review solely for “could use more tests” when the implementation looks correct — unless required tests are entirely absent.

## Step 6 — Per-card summary

End with a short line per card: `PASS` or `FAIL` plus the result file path when failing. If tests were added, append `+tests`.
