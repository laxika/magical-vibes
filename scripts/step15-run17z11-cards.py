#!/usr/bin/env python3
"""Step 15, run 17z11: the abilities the collectors bound event data to by their effect's class read the event.

- Rage Forger ("you may have that creature deal 1 damage to target player or planeswalker"): `CombatAttackService` made
  the attacking creature the target choice's source permanent when the "you may" wrapped a
  `DealDamageToTargetPlayerOrPlaneswalkerEffect`. "That creature" is the attacking creature the trigger event was about,
  the entry's triggering permanent, which `DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature` deals the
  damage through (as Stalking Vengeance's "it deals damage").
- Forbidden Orchard ("target opponent creates a 1/1 colorless Spirit creature token"): the land-tap collector bound
  `getOpponentId`'s one opponent without a choice; the ability now chooses its target, which the effect's player
  relation restricts to an opponent.

Creepy Puppeteer, Whispering Specter, Treasure Nabber and Karplusan Minotaur keep their forms.

Idempotent, per letter module.

Usage: step15-run17z11-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

RAGE_FORGER = [
    ("""                        new MayEffect(
                                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                                "Deal 1 damage to target player or planeswalker?"));""",
     """                        new MayEffect(
                                DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature(new Fixed(1)),
                                "Deal 1 damage to target player or planeswalker?"));"""),
    ("""        // creature deal 1 damage to target player or planeswalker. The player/planeswalker target
        // is restricted by the card-level filter; the attacking creature is the damage source.""",
     """        // creature deal 1 damage to target player or planeswalker. The player/planeswalker target
        // is restricted by the card-level filter; the attacking creature, the trigger event's, deals the damage."""),
    ("import com.github.laxika.magicalvibes.model.CounterType;\n",
     "import com.github.laxika.magicalvibes.model.CounterType;\nimport com.github.laxika.magicalvibes.model.amount.Fixed;\n"),
]

FORBIDDEN_ORCHARD = [
    ("""        addTrigger(Triggers.playerTapsLandForMana(), new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Spirit", 1, 1, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of())
        ));""",
     """        addTrigger(Triggers.playerTapsLandForMana(), new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Spirit", 1, 1, null,
                        List.of(CardSubtype.SPIRIT), Set.of(), Set.of()),
                PlayerRelation.OPPONENT
        ));"""),
    ("import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;\n",
     "import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;\n"
     "import com.github.laxika.magicalvibes.model.filter.PlayerRelation;\n"),
]

CARDS = {
    "f": {"ForbiddenOrchard": FORBIDDEN_ORCHARD},
    "r": {"RageForger": RAGE_FORGER},
}


def convert(path, replacements):
    text = path.read_text()
    if all(new in text for _, new in replacements):
        return False
    for old, new in replacements:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, replacements in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, replacements) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
