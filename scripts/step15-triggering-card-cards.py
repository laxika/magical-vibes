#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 16: the triggering card and player are the stack entry's.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-triggering-card-cards.py "$m"; done

No collector keys a trigger condition on an effect's class any more:

- Mazzy, Truesword Paladin: "Whenever an Aura you control is put into your graveyard from the battlefield" is the
  trigger's filter (an Aura, inside the Aura-or-Equipment filter its watch is read by), instead of a class-keyed
  collector handler that skipped Equipment.
- Mind's Dilation: "Whenever an opponent casts their first spell each turn" is the trigger's nthThisTurn(1), which the
  generic cast collector judges, instead of a class-keyed collector handler that counted the spells.
"""
import pathlib
import sys

CARDS = {
    "MazzyTrueswordPaladin.java": [
        ("import com.github.laxika.magicalvibes.model.Card;\n",
         "import com.github.laxika.magicalvibes.model.Card;\n"
         "import com.github.laxika.magicalvibes.model.CardSubtype;\n"),
        ("import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;\n"),
        ("        addTrigger(Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL),\n",
         "        // \"Whenever an Aura you control is put into your graveyard from the battlefield\".\n"
         "        addTrigger(Triggers.dies(new PermanentAllOfPredicate(List.of(\n"
         "                                Triggers.AURA_OR_EQUIPMENT, new PermanentHasSubtypePredicate(CardSubtype.AURA))),\n"
         "                        ObjectRelation.YOU_CONTROL),\n"),
    ],
    "MindsDilation.java": [
        ("        addTrigger(Triggers.opponentCastsSpell(),\n",
         "        // \"Whenever an opponent casts their first spell each turn\".\n"
         "        addTrigger(Triggers.opponentCastsSpell().nthThisTurn(1),\n"),
    ],
}


def convert(path, replacements):
    src = path.read_text()
    converted = src
    for old, new in replacements:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main():
    module = pathlib.Path(sys.argv[1])
    for path in sorted(module.glob("src/main/java/**/*.java")):
        if path.name in CARDS:
            convert(path, CARDS[path.name])


if __name__ == "__main__":
    main()
