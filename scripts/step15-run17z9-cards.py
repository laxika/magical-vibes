#!/usr/bin/env python3
"""Step 15, run 17z9: the cast triggers' "that player" is the triggering player the generic cast collector records.

`SpellCastTriggerCollectorService`'s class-keyed handlers for unfiltered cast triggers bound the caster as the entry's
`targetId` by effect class (`ReturnPermanentControlledByPlayerToHandEffect`, `DiscardEffect`,
`LoseLifeUnlessDiscardEffect`, `DealDamageToPlayersEffect`, `DrawCardForTargetPlayerEffect`, `MillEffect`,
`LoseLifeUnlessPaysEffect`, `DamageUnlessPaysEffect`, `DrawCardUnlessPaysEffect`), and the last three judged a spell
filter carried by the effect. The cards now write filtered cast triggers (`anySpell()`, or the spell filter the effect
carried as the trigger's `withFilter`), which the generic cast collector puts on the stack as one non-targeting object
with the caster as "that player" (the entry's non-targeting player, which the `TRIGGERING_PLAYER` recipients read) and
the spell as its triggering spell. The recipients that read "target player" become `TRIGGERING_PLAYER`.

Idempotent, per letter module ("shared" for the cards another letter's card constructs).

Usage: step15-run17z9-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

CARDS = {
    "m": {
        "ManaBreach": ([("        addTrigger(Triggers.playerCastsSpell(),\n",
                         "        addTrigger(Triggers.playerCastsSpell().anySpell(),\n")], []),
        "MemoryErosion": ([("        addTrigger(Triggers.opponentCastsSpell(), new MillEffect(2, MillRecipient.TARGET_PLAYER));",
                            "        addTrigger(Triggers.opponentCastsSpell().anySpell(),\n"
                            "                new MillEffect(2, MillRecipient.TRIGGERING_PLAYER));")], []),
        "MysticRemora": ([("""        addTrigger(Triggers.opponentCastsSpell(),
                new DrawCardUnlessPaysEffect(1, 4,
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));""",
                           """        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new DrawCardUnlessPaysEffect(1, 4));""")], []),
    },
    "o": {
        "Oppression": ([("""        // The Triggers.playerCastsSpell() collector stamps the casting player onto the entry's
        // targetId, so the TARGET_PLAYER discard lands on whoever cast the spell.
        addTrigger(Triggers.playerCastsSpell(), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));""",
                         """        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new DiscardEffect(1, DiscardRecipient.TRIGGERING_PLAYER, false));""")], []),
    },
    "p": {
        "PainfulQuandary": ([("        addTrigger(Triggers.opponentCastsSpell(), new LoseLifeUnlessDiscardEffect(5));",
                              "        addTrigger(Triggers.opponentCastsSpell().anySpell(), new LoseLifeUnlessDiscardEffect(5));")],
                            []),
    },
    "f": {
        "ForcedFruition": ([("        addTrigger(Triggers.opponentCastsSpell(), new DrawCardForTargetPlayerEffect(7));",
                             "        addTrigger(Triggers.opponentCastsSpell().anySpell(), new DrawCardForTargetPlayerEffect(7));")],
                           []),
    },
    "i": {
        "IsolationCell": ([("""        addTrigger(Triggers.opponentCastsSpell(),
                new LoseLifeUnlessPaysEffect(2, 2, new CardTypePredicate(CardType.CREATURE)));""",
                            """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                LoseLifeUnlessPaysEffect.forTriggeringPlayer(2, 2));""")], []),
    },
    "s": {
        "SoulBarrier": ([("""        addTrigger(Triggers.opponentCastsSpell(),
                new DamageUnlessPaysEffect(2, 2, new CardTypePredicate(CardType.CREATURE)));""",
                          """        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new DamageUnlessPaysEffect(2, 2));""")], []),
    },
    "r": {
        "RhysticStudy": ([("        addTrigger(Triggers.opponentCastsSpell(), new DrawCardUnlessPaysEffect(1, 1));",
                           "        addTrigger(Triggers.opponentCastsSpell().anySpell(), new DrawCardUnlessPaysEffect(1, 1));")],
                         []),
    },
    "shared": {
        "t/TovolarsMagehunter": ([(
            "        addTrigger(Triggers.opponentCastsSpell(), new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));",
            "        addTrigger(Triggers.opponentCastsSpell().anySpell(),\n"
            "                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));")], []),
    },
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


def card_path(letter, name):
    if "/" in name:
        sub, name = name.split("/")
    else:
        sub = letter
    return (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/") / f"cards/{sub}/{name}.java")


def main(letters):
    for letter in letters:
        for name, (replacements, imports) in CARDS.get(letter, {}).items():
            path = card_path(letter, name)
            print(f"{name}: {'converted' if convert(path, replacements, imports) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
