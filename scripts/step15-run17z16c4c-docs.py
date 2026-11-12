#!/usr/bin/env python3
"""Step 15, run 17z16c4c: agent-docs follow the answer of a resolving "you may" that no longer chooses a target.

Rewrites the lines of `agent-docs/` that describe `MayAbilityHandlerService`'s resolution-time target choice, which is
gone: a triggered ability's targets are chosen as it is put on the stack (CR 603.3d), its "you may" is decided as it
resolves (CR 603.5), and the answer chooses nothing.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c4c-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ARCHITECTURE.md": [
        ("the graveyard target-selection helpers, the CR 603.5 resolution-time may path, and one guarded conditional",
         "the CR 603.5 resolution-time may path (`handleResolutionTimeMayChoice`: it pays the question's costs and "
         "resumes the resolution that asked with the answer; it chooses no target and no graveyard card, "
         "effect-slot-refactor.md step 15, run 17z16c4c: a triggered ability's targets, the one its optional effect "
         "acts on included, are chosen as its collector puts it on the stack, CR 603.3d, and are only checked again "
         "as it resolves, CR 608.2b, so a \"you may [do X to] target ...\" that reaches the stack with no target "
         "resolves without one; route it to its target choice in the collector, never in the answer), and one "
         "guarded conditional"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("""When `MayAbilityHandlerService` performs a resolution-time target choice for one of those later
effects, `mayAbilityPermanentTargets` uses this precedence:

1. the card-level `TargetFilter` (`mayAbilityTargetFilter`, which prefers the effect's own declared
   target group over the card's primary filter), **and**
2. the effect's own `PermanentPredicate` (`EffectResolution.targetPredicateOf`), if it carries one;
3. only when the ability declares **neither** does the effect's `TargetSpec` restrict anything — and
   then it is evaluated by `TargetPredicateEvaluationService`, the same shared evaluator cast-time
   validation uses.

A card-level filter therefore *replaces* the spec's own type restriction here; it does not stack with
it. Arm 3 used to be an open-coded target-category switch whose `default` rejected every permanent,
so a bare `land()` or `playerOrPlaneswalker()` spec found no legal target at all (Boggart Shenanigans
never offered a planeswalker).
""",
         """Answering the "you may" never chooses a target (effect-slot-refactor.md step 15, run 17z16c4c):
`MayAbilityHandlerService` has no resolution-time target or graveyard choice any more. An ability whose
"you may [do X to] target ..." reached the stack with no target resolves without one (CR 603.3d, 608.2b),
so a new trigger route must send a targeting `MayEffect` to its target choice as the ability goes on the
stack, like any other targeting effect. A card a resolving effect has its controller *choose* (no
"target" in the text: "choose an instant or sorcery card in your graveyard", "you may cast a spell from
your graveyard") is chosen by that effect's own handler as it resolves.
"""),
    ],
}


def main():
    failed = False
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                print(f"{name}: edit does not apply ({text.count(old)} matches): {old[:70]!r}", file=sys.stderr)
                failed = True
                continue
            text = text.replace(old, new)
        path.write_text(text)
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
