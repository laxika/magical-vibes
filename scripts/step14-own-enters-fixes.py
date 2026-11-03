#!/usr/bin/env python3
"""Exact replacements for the own enters abilities effect-slot-refactor.md step 14 run 4c decided on its own.

- Armored Kincaller: "you may reveal a Dinosaur card from your hand. If you do or if you control another Dinosaur,
  you gain 3 life" is one ability whose "if" is no intervening "if": the two abilities, the second gated on
  controlling another Dinosaur as it triggered, become one with both checks made as it resolves.
- Kitnap: gift's "when this permanent enters, if its gift cost was paid" (CR 702.174b) is the option, as on the
  other gift cards run 4b converted.

Each replacement must match exactly once, or already be applied (then it is skipped).

Usage: python3 scripts/step14-own-enters-fixes.py [--dry-run]
"""
import difflib
import sys

FIXES = {
    "magical-vibes-card/a/src/main/java/com/github/laxika/magicalvibes/cards/a/ArmoredKincaller.java": [(
        """        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayRevealSubtypeFromHandEffect(
                CardSubtype.DINOSAUR,
                new ConditionalEffect(new NotCondition(controlsAnotherDinosaur), new GainLifeEffect(3)),
                "Reveal a Dinosaur card from your hand?"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(controlsAnotherDinosaur, new GainLifeEffect(3)));""",
        """        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayRevealSubtypeFromHandEffect(
                        CardSubtype.DINOSAUR,
                        ConditionalEffect.unless(new NotCondition(controlsAnotherDinosaur), new GainLifeEffect(3)),
                        "Reveal a Dinosaur card from your hand?"),
                ConditionalEffect.unless(controlsAnotherDinosaur, new GainLifeEffect(3)));""",
    )],
    "magical-vibes-card/k/src/main/java/com/github/laxika/magicalvibes/cards/k/Kitnap.java": [(
        """        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                new ConditionalEffect(new GiftPromised(), new EachOtherPlayerDrawsCardEffect(1)));""",
        """        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new GiftPromised()),
                new EachOtherPlayerDrawsCardEffect(1));""",
    ), (
        "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n",
        "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
        "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n",
    )],
}


def main():
    dry_run = "--dry-run" in sys.argv
    for path, replacements in FIXES.items():
        src = open(path).read()
        out = src
        for old, new in replacements:
            if out.count(new) == 1 and out.count(old) == 0:
                continue
            if out.count(old) != 1:
                sys.exit(f"{path}: expected one match, found {out.count(old)}:\n{old}")
            out = out.replace(old, new)
        if out == src:
            print(f"{path}: already applied")
            continue
        if dry_run:
            sys.stdout.writelines(difflib.unified_diff(src.splitlines(True), out.splitlines(True), path, path))
        else:
            open(path, "w").write(out)
            print(f"{path}: fixed")


if __name__ == "__main__":
    main()
