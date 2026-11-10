#!/usr/bin/env python3
"""Step 15, run 17z15b: the cards write what two collectors knew by an effect's class (row 17z15c).

`SpellCastTriggerCollectorService.handleGenericSpellCastTrigger` judged a trigger condition for any ability with a
`GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect`, and `UnattachTriggerSupport` built "whenever this
Equipment becomes unattached from a permanent, destroy / sacrifice that permanent" from the static markers
`DestroyOnUnattachEffect` and `SacrificeOnUnattachEffect`. The cards write them (Oracle text checked with the Scryfall
MCP):

- Boreal Outrider (KHM 163): "Whenever you cast a creature spell, if {S} of any of that spell's colors was spent to
  cast it, that creature enters with an additional +1/+1 counter on it." The "if" is the trigger's stack-entry filter
  `StackEntrySnowManaOfItsColorSpentPredicate` (what was spent can't change once the spell is cast, as Void Mirror
  writes "if no colored mana was spent to cast it"), the effect the existing
  `GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(new Fixed(1))`.
- Captain's Hook (RIX 177): "Whenever this Equipment becomes unattached from a permanent, destroy that permanent."
- Grafted Wargear (5DN 126), Grafted Exoskeleton (SOM 162), Stitcher's Graft (EMN 200): "Whenever this Equipment
  becomes unattached from a permanent, sacrifice that permanent."

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z15b-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

EFFECT = "import com.github.laxika.magicalvibes.model.effect."
TRIGGERS = "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"
SACRIFICE = ("        addStaticEffect(new SacrificeOnUnattachEffect());\n",
             "        addTrigger(Triggers.becomesUnattachedFromPermanent(), new SacrificeTriggeringPermanentEffect());\n")

EDITS = {
    "BorealOutrider": [
        ("""        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect());
""",
         """        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE))
                        .withStackEntryFilter(new StackEntrySnowManaOfItsColorSpentPredicate()),
                new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(new Fixed(1)));
"""),
        (EFFECT + "GrantAdditionalPlusOnePlusOneCounterToSnowPaidCreatureSpellEffect;\n",
         "import com.github.laxika.magicalvibes.model.amount.Fixed;\n"
         + EFFECT + "GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;\n"),
        ("import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.StackEntrySnowManaOfItsColorSpentPredicate;\n"),
    ],
    "CaptainsHook": [
        ("        addStaticEffect(new DestroyOnUnattachEffect());\n",
         "        addTrigger(Triggers.becomesUnattachedFromPermanent(),\n"
         "                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));\n"),
        (EFFECT + "DestroyOnUnattachEffect;\n", EFFECT + "DestroyReferencedPermanentEffect;\n"),
        (EFFECT + "GrantSubtypeEffect;\n", EFFECT + "GrantSubtypeEffect;\n" + EFFECT + "PermanentReference;\n"),
        (EFFECT + "StaticBoostEffect;\n", EFFECT + "StaticBoostEffect;\n" + TRIGGERS),
    ],
    "GraftedWargear": [
        SACRIFICE,
        (EFFECT + "SacrificeOnUnattachEffect;\n", EFFECT + "SacrificeTriggeringPermanentEffect;\n"),
        (EFFECT + "StaticBoostEffect;\n", EFFECT + "StaticBoostEffect;\n" + TRIGGERS),
    ],
    "GraftedExoskeleton": [
        SACRIFICE,
        (EFFECT + "SacrificeOnUnattachEffect;\n",
         EFFECT + "SacrificeTriggeringPermanentEffect;\n" + TRIGGERS),
    ],
    "StitchersGraft": [
        SACRIFICE,
        (EFFECT + "SacrificeOnUnattachEffect;\n", EFFECT + "SacrificeTriggeringPermanentEffect;\n"),
    ],
}


def main() -> int:
    letters = {letter.lower() for letter in sys.argv[1:]}
    failures = []
    changed = 0
    for name, replacements in EDITS.items():
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / CARDS.format(letter=letter, name=name)
        text = path.read_text()
        original = text
        for old, new in replacements:
            if new in text and (old not in text or old in new):
                continue
            if text.count(old) != 1:
                failures.append(f"{name}: {old.strip()[:60]!r} occurs {text.count(old)} times")
                continue
            text = text.replace(old, new)
        if text != original:
            path.write_text(text)
            changed += 1
    for failure in failures:
        print("FAILED", failure)
    print(f"{changed} card files changed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
