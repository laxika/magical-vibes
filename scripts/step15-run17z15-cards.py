#!/usr/bin/env python3
"""Step 15, run 17z15: "that creature" of a lone "you may" on "whenever a creature you control attacks".

`CombatAttackService.putAllyCreatureAttackAbilityOnStack` put an ability that is only an untargeted "you may" on the
stack with the attacking creature as its source permanent, so two cards wrote "it" / "that creature" as effects on the
source. The ability's source is its owner now, as for every other ability of the trigger, and the attacking creature
is the entry's triggering permanent, which the effects read (Oracle text checked with the Scryfall MCP):

- Shadow Puppeteers (WOC 12): "you may have it become a red Dragon with base power and toughness 4/4 in addition to
  its other colors and types until end of turn".
- Assaultron Dominator (PIP 54): "put your choice of a +1/+1, first strike, or trample counter on that creature".

`CombatDamageService` put a lone untargeted "you may" on the stack without the creature's snapshot, which every other
"deals combat damage to a player" ability records (its last known information). One card's payment relied on the gap:

- Mercurial Spelldancer (ONE 61): "you may remove two oil counters from it. If you do, ..." can be paid only while
  it is on the battlefield, so its condition says so (`SourceIsOnBattlefield`) instead of reading the counters it
  last had.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z15-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

EDITS = {
    "ShadowPuppeteers": [
        ("""                                new AnimatePermanentsEffect(4, 4,
                                        List.of(CardSubtype.DRAGON), Set.of()),
                                new GrantColorUntilEndOfTurnEffect(
                                        CardColor.RED, true, GrantScope.SELF)),
""",
         """                                new AnimatePermanentsEffect(4, 4,
                                        List.of(CardSubtype.DRAGON), Set.of(), null, Set.of(),
                                        GrantScope.TRIGGERING_PERMANENT, EffectDuration.UNTIL_END_OF_TURN),
                                new GrantColorUntilEndOfTurnEffect(
                                        CardColor.RED, true, GrantScope.TRIGGERING_PERMANENT)),
"""),
        ("import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.EffectDuration;\n"),
    ],
    "MercurialSpelldancer": [
        ("""                        new SourceCounterThreshold(2, CounterType.OIL),
""",
         """                        new AllOf(List.of(new SourceIsOnBattlefield(),
                                new SourceCounterThreshold(2, CounterType.OIL))),
"""),
        ("import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;\n",
         "import com.github.laxika.magicalvibes.model.condition.AllOf;\n"
         "import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;\n"
         "import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n"),
    ],
    "AssaultronDominator": [
        ("new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)",
         "PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, 1)"),
        ("new PutCountersOnSelfEffect(CounterType.FIRST_STRIKE)",
         "PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.FIRST_STRIKE, 1)"),
        ("new PutCountersOnSelfEffect(CounterType.TRAMPLE)",
         "PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.TRAMPLE, 1)"),
        ("import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;\n"),
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
