#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 15: the triggering permanent is the stack entry's triggering permanent.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-triggering-permanent-cards.py "$m"; done

No effect is rebuilt with the permanent, card or land a trigger was about bound in, and no trigger condition is the
effect's own component:

- Raggadragga, Goreguts Boss and Tiller Engine: UntapPermanentsEffect(TapUntapScope.TRIGGERING) for the removed
  UntapTriggeringPermanentEffect (the same resolution: untap the entry's triggering permanent).
- Sacred Ground and Hedge Shredder: ReturnTriggeringLandFromGraveyardToBattlefieldEffect lost its bound land card id and
  graveyard entry version; it reads the entry's triggering card (Sacred Ground) or event cards (Hedge Shredder).
- Raking Canopy, Barbed Foliage, Circle of Flame, Palazzo Archers and Storm Queen of Wakanda: the attacker condition of
  DealDamageToTriggeringAttackerEffect ("a creature with flying", "a creature without flying") is the trigger's
  filter, which the "attacks you" collector judges as the creature is declared an attacker.
- Mirror of Life Trapping: "if it was cast" is the trigger's filter (PermanentWasCastPredicate), as Rapid Augmenter
  writes "if it wasn't cast", instead of a class-keyed collector handler.
"""
import pathlib
import re
import sys

UNTAP = [
    ("import com.github.laxika.magicalvibes.model.effect.UntapTriggeringPermanentEffect;\n", ""),
    ("new UntapTriggeringPermanentEffect()", "new UntapPermanentsEffect(TapUntapScope.TRIGGERING)"),
]

CARDS = {
    "RaggadraggaGoregutsBoss.java": UNTAP,
    "TillerEngine.java": [
        ("import com.github.laxika.magicalvibes.model.effect.TapUntapScope;\n"
         "import com.github.laxika.magicalvibes.model.effect.UntapTriggeringPermanentEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.TapUntapScope;\n"
         "import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;\n"),
        ("new UntapTriggeringPermanentEffect()", "new UntapPermanentsEffect(TapUntapScope.TRIGGERING)"),
    ],
    "SacredGround.java": [
        ("""        // graveyard from the battlefield, return that card to the battlefield. The trigger collector
        // stamps the concrete land card id at collection time.
""",
         """        // graveyard from the battlefield, return that card to the battlefield. "That card" is the stack entry's
        // triggering card.
"""),
        ("new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null)",
         "new ReturnTriggeringLandFromGraveyardToBattlefieldEffect()"),
    ],
    "HedgeShredder.java": [
        ("new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null, true)",
         "new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(true)"),
    ],
    "MirrorOfLifeTrapping.java": [
        ("import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureUntilSourceLeavesAndReturnOthersEffect;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentWasCastPredicate;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n"),
        ("        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),\n",
         "        // \"Whenever a creature enters, if it was cast\": whether it was cast can't change, so it is the filter.\n"
         "        addTrigger(Triggers.enters(\n"
         "                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentWasCastPredicate())),\n"
         "                        ObjectRelation.ANOTHER),\n"),
    ],
}

ATTACKER_DAMAGE_CARDS = (
    "RakingCanopy.java",
    "BarbedFoliage.java",
    "CircleOfFlame.java",
    "PalazzoArchers.java",
    "StormQueenOfWakanda.java",
)


def convert(path, replacements):
    src = path.read_text()
    converted = src
    for old, new in replacements:
        if new and new in converted:
            continue
        if old not in converted:
            if not new:
                continue
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def split_top_level(args):
    parts, depth, start = [], 0, 0
    for i, ch in enumerate(args):
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        elif ch == "," and depth == 0:
            parts.append(args[start:i])
            start = i + 1
    parts.append(args[start:])
    return parts


def closing_paren(src, open_index):
    depth = 0
    for i in range(open_index, len(src)):
        if src[i] == "(":
            depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0:
                return i
    raise ValueError("unbalanced parentheses")


def convert_attacker_damage(path):
    """addTrigger(Triggers.X(), new DealDamageToTriggeringAttackerEffect(amount, condition)) becomes
    addTrigger(Triggers.X().withFilter(condition), new DealDamageToTriggeringAttackerEffect(amount))."""
    src = path.read_text()
    marker = "new DealDamageToTriggeringAttackerEffect("
    start = src.find(marker)
    if start < 0:
        raise SystemExit(f"{path}: no DealDamageToTriggeringAttackerEffect")
    open_index = start + len(marker) - 1
    close_index = closing_paren(src, open_index)
    args = split_top_level(src[open_index + 1:close_index])
    if len(args) == 1:
        return
    if len(args) != 2:
        raise SystemExit(f"{path}: unexpected arguments {args!r}")
    amount, condition = args[0].strip(), " ".join(args[1].split())
    trigger = re.search(r"addTrigger\((Triggers\.\w+\(\)),\s*$", src[:start])
    if trigger is None:
        raise SystemExit(f"{path}: the trigger isn't a plain Triggers call before the effect")
    line_start = src.rfind("\n", 0, trigger.start()) + 1
    indent = src[line_start:trigger.start()]
    filtered = trigger.group(1) + ".withFilter(" + condition + ")"
    if len(indent) + len("addTrigger(") + len(filtered) + 1 > 120:
        filtered = trigger.group(1) + "\n" + indent + " " * 16 + ".withFilter(" + condition + ")"
    converted = (src[:trigger.start(1)] + filtered
                 + src[trigger.end(1):start] + marker + amount + ")" + src[close_index + 1:])
    path.write_text(converted)
    print(f"converted {path}")


def main(module):
    for path in sorted(pathlib.Path(module).rglob("*.java")):
        if "/build/" in str(path) or "/src/main/" not in str(path):
            continue
        if path.name in CARDS:
            convert(path, CARDS[path.name])
        elif path.name in ATTACKER_DAMAGE_CARDS:
            convert_attacker_damage(path)


if __name__ == "__main__":
    main(sys.argv[1])
