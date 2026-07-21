---
name: review-card
description: Review one or more already-implemented Magic cards in the magical-vibes engine from a set code and collector number(s) (e.g. "review DKA 76", "review SOS 1 2 3"). Checks oracle fidelity, effect reuse, targeting, and tests. Writes findings only when issues exist.
---

# Review a card

Input is a **set code + one or more collector numbers** from that set (e.g. `DKA 76`, or `SOS 1 2 3 4`). If the user gives a card name instead, look up the set/collector number first.

**Multiple collector numbers:** review each card independently, one at a time — finish Steps 1–5 fully for one collector number before moving to the next. Keep each review's context small.

This is a **read-only review**. Do **not** edit card classes, tests, effects, or docs unless the user explicitly asks you to fix findings afterward. Do not commit.

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

## Step 4 — Verdict and result file

Normalize set code to uppercase for the path. Collector number is the numeric id as given (e.g. `76`).

Result path:

```
scripts/result/<SET>/<COLLECTOR_NUMBER>.txt
```

- **Pass (no errors):** do **not** create or keep a result file. If a stale `scripts/result/<SET>/<COLLECTOR_NUMBER>.txt` exists from a previous review, **delete it**. Reply with a one-line pass summary only.
- **Fail (any issue):** create `scripts/result/<SET>/` if needed and write `scripts/result/<SET>/<COLLECTOR_NUMBER>.txt` with a concise report:
  - card name, set, collector number
  - class / test paths
  - bullet list of issues (what is wrong, why it matters vs oracle/rules/conventions)
  - optional short suggested fix direction (no code edits)

Severity: only real correctness / convention problems. Do not nitpick style, comments, or naming unless they cause wrong behavior or violate CLAUDE.md testing/implementation rules.

## Step 5 — Per-card summary

End with a short line per card: `PASS` or `FAIL` plus the result file path when failing.
