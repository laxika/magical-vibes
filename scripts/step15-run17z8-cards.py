#!/usr/bin/env python3
"""Step 15, run 17z8: the "choose one that hasn't been chosen" abilities carry their own modal ability.

The collectors built a `ChooseOneEffect` from the options of `ChooseModeNotYetChosenEffect`,
`ChooseModeNotYetChosenThisTurnEffect` and `ChooseModeNotYetChosenThisTurnOnSpellCastEffect` by their class. Each card
now writes the modal itself, its restriction a property of the modal (`ChooseOneEffect.notYetChosen`,
`.notYetChosenThisTurn`, `.notChosenDuringLastCombat`), judged where the mode is chosen as the ability goes on the stack
(CR 603.3c, 700.2b). The cast record's filters are the cast trigger's (`withFilter`, `withStackEntryFilter`).

By Oracle text: Lita, Little Orphan Amphibian's "hasn't been chosen this turn" (its collector already reset the record
each turn); By Elspeth's Command's "wasn't chosen during your last combat" (a rules fix: "this turn" let it repeat the
mode of the previous turn's combat).

Idempotent, per letter module.

Usage: step15-run17z8-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

NOT_YET = ("new ChooseModeNotYetChosenEffect(", "ChooseOneEffect.notYetChosen(")
NOT_YET_AS_THIS_TURN = ("new ChooseModeNotYetChosenEffect(", "ChooseOneEffect.notYetChosenThisTurn(")
THIS_TURN = ("new ChooseModeNotYetChosenThisTurnEffect(", "ChooseOneEffect.notYetChosenThisTurn(")
LAST_COMBAT = ("new ChooseModeNotYetChosenThisTurnEffect(", "ChooseOneEffect.notChosenDuringLastCombat(")

VISION = ("""        addTrigger(Triggers.youCastSpell(),
                new ChooseModeNotYetChosenThisTurnOnSpellCastEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(""",
          """        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                ChooseOneEffect.notYetChosenThisTurn(
                        List.of(""")
# Its own "when this enters" modal is chosen as the ability goes on the stack, never declared with the creature spell
# (a triggered ability's modes don't make the spell modal, CR 700.2), so it keeps the trigger-time wrapper.
FANTASTIC_FOUR_ETB = ("                new ChooseModeNotYetChosenThisTurnEffect(options));",
                      "                new ChooseOneAtTriggerTimeEffect(ChooseOneEffect.notYetChosenThisTurn(options)));")
FANTASTIC_FOUR_CAST = ("""        addTrigger(Triggers.youCastSpell(),
                new ChooseModeNotYetChosenThisTurnOnSpellCastEffect(
                        null,
                        new StackEntryManaValuePowerOrToughnessEqualsPredicate(4),
                        options));""",
                       """        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryManaValuePowerOrToughnessEqualsPredicate(4)),
                ChooseOneEffect.notYetChosenThisTurn(options));""")

CARDS = {
    "b": {"ByElspethsCommand": [LAST_COMBAT], "BreechesEagerPillager": [THIS_TURN]},
    "c": {"CaptiveAudience": [NOT_YET]},
    "d": {"DemonicPact": [NOT_YET]},
    "g": {"GollumRiddleMaster": [NOT_YET], "GaladrielLightOfValinor": [THIS_TURN], "GalaGreeters": [THIS_TURN]},
    "h": {"HenrikaDomnathi": [NOT_YET]},
    "k": {"KimoyoBeads": [NOT_YET]},
    "l": {"LitaLittleOrphanAmphibian": [NOT_YET_AS_THIS_TURN]},
    "m": {"MonumentToEndurance": [THIS_TURN]},
    "p": {"ParapetThrasher": [THIS_TURN]},
    "s": {"SolKanarTheTainted": [NOT_YET], "SilentHallcreeper": [NOT_YET]},
    "t": {"TheVision": [VISION], "TheFantasticFour": [FANTASTIC_FOUR_ETB, FANTASTIC_FOUR_CAST], "TevalsJudgment": [THIS_TURN]},
    "z": {"ZukoConflicted": [NOT_YET]},
}

RECORDS = ["ChooseModeNotYetChosenEffect", "ChooseModeNotYetChosenThisTurnEffect",
           "ChooseModeNotYetChosenThisTurnOnSpellCastEffect"]
COMMENTS = {
    "        // At the beginning of your upkeep, choose one that hasn't been chosen. The mode (and its\n"
    "        // targets) are picked as the trigger goes on the stack; each chosen mode is consumed, so the\n"
    "        // fourth upkeep forces whichever mode is left.\n":
    "        // At the beginning of your upkeep, choose one that hasn't been chosen. The mode (and its\n"
    "        // targets) are picked as the ability goes on the stack; each chosen mode is used up, so the\n"
    "        // fourth upkeep forces whichever mode is left.\n",
    "        // Whenever another creature you control enters, choose one that hasn't been chosen this\n"
    "        // turn. The turn-scoped modal tracks each selected mode on Galadriel's permanent.\n":
    "        // Whenever another creature you control enters, choose one that hasn't been chosen this\n"
    "        // turn: each mode chosen is recorded on Galadriel's permanent as the ability goes on the stack.\n",
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


def convert(path, replacements):
    text = path.read_text()
    if not any(f"new {record}(" in text for record in RECORDS):
        return False
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    for record in RECORDS:
        if f"new {record}(" in text:
            raise SystemExit(f"{path}: {record} is still constructed")
        text = text.replace(f"import {PKG}.model.effect.{record};\n", "")
    for old, new in COMMENTS.items():
        text = text.replace(old, new)
    text = add_import(text, f"import {PKG}.model.effect.ChooseOneEffect;\n")
    if "new ChooseOneAtTriggerTimeEffect(" in text:
        text = add_import(text, f"import {PKG}.model.effect.ChooseOneAtTriggerTimeEffect;\n")
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
