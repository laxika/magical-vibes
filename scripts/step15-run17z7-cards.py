#!/usr/bin/env python3
"""Step 15, run 17z7: the class-keyed death, graveyard and life-gain rewrites go; the cards write their abilities as printed.

- Lynde, Cheerful Tormentor: the collector rebuilt `RegisterDelayedReturnCurseAttachedToPlayerEffect(cardId, controller)`
  with the dying Curse's id and Lynde's controller; the effect is fieldless and reads the stack entry's triggering card
  (its graveyard stay, CR 400.7) and controller as it resolves.
- Tiana, Ship's Caretaker: the collector rebuilt `RegisterDelayedReturnCardFromGraveyardToHandEffect(true)` from the
  card's `()`; the card writes the optional ("you may return that card") form itself.
- Purgatory: "whenever a nontoken creature is put into your graveyard from the battlefield" was "a nontoken creature
  dies" plus a class-keyed handler that checked the card was in its controller's graveyard; it is the trigger
  `Triggers.dies(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)`, judged by the trigger pipeline.
- Sanguine Bond, Vito, Thorn of the Dusk Rose, Defiant Bloodlord, Enduring Tenacity: "target opponent loses that much
  life" took the engine's one opponent without a target choice (the class-keyed life-gain handler); the ability declares
  its target, an opponent, which the "you gain life" collector's default handler has chosen as the ability goes on the
  stack (CR 603.3d; a rules fix in multiplayer games, where each trigger may target a different opponent).

Idempotent, per letter module.

Usage: step15-run17z7-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

LYNDE = ("new RegisterDelayedReturnCurseAttachedToPlayerEffect(null, null)",
         "new RegisterDelayedReturnCurseAttachedToPlayerEffect()")
TIANA = ("                new RegisterDelayedReturnCardFromGraveyardToHandEffect());",
         "                new RegisterDelayedReturnCardFromGraveyardToHandEffect(true));")
PURGATORY = ("        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANY),",
             "        addTrigger(Triggers.dies(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN),")

OPPONENT = """        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.youGainLife(),"""
SANGUINE = ("""        // "Whenever you gain life, target opponent loses that much life." The trigger collector
        // snapshots the life gained onto the entry's event value; EventValue reads it at resolution.
        addTrigger(Triggers.youGainLife(), new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));""",
            """        // "Whenever you gain life, target opponent loses that much life." The opponent is chosen as the ability goes
        // on the stack; the life gained is the entry's event value, which EventValue reads as it resolves.
""" + OPPONENT + """
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));""")
TARGET_OPPONENT = ("""        addTrigger(Triggers.youGainLife(),
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));""",
                   OPPONENT + """
                new LoseLifeEffect(new EventValue(), LoseLifeRecipient.TARGET_PLAYER));""")
OPPONENT_IMPORTS = ["model.filter.PlayerPredicateTargetFilter", "model.filter.PlayerRelation",
                    "model.filter.PlayerRelationPredicate"]

CARDS = {
    "d": {"DefiantBloodlord": ([TARGET_OPPONENT], OPPONENT_IMPORTS)},
    "e": {"EnduringTenacity": ([TARGET_OPPONENT], OPPONENT_IMPORTS)},
    "l": {"LyndeCheerfulTormentor": ([LYNDE], [])},
    "p": {"Purgatory": ([PURGATORY], [])},
    "s": {"SanguineBond": ([SANGUINE], OPPONENT_IMPORTS)},
    "t": {"TianaShipsCaretaker": ([TIANA], [])},
    "v": {"VitoThornOfTheDuskRose": ([TARGET_OPPONENT], OPPONENT_IMPORTS)},
}


def add_import(text, line):
    if line in text:
        return text
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
    target = line.strip()
    for i in imports:
        if lines[i].strip() > target and not lines[i].startswith("import java."):
            lines.insert(i, line)
            return "".join(lines)
    java = [i for i in imports if lines[i].startswith("import java.")]
    lines.insert(java[0] if java else imports[-1] + 1, line)
    return "".join(lines)


def convert(path, replacements, imports):
    text = path.read_text()
    if all(new in text for _, new in replacements):
        return False
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    for name in imports:
        text = add_import(text, f"import {PKG}.{name};\n")
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, (replacements, imports) in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, replacements, imports) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
