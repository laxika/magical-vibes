#!/usr/bin/env python3
"""Moves card classes off the "another object enters" EffectSlots of step 12a, part 2 (effect-slot-refactor.md).

Every registration becomes a triggered ability with a real ZoneChangeTrigger to the battlefield. The triggers are the
keys of the slot's ZoneChangeWatch in the engine, so the two must stay the same. Each slot keeps the relation its
collector gave it (the collectors' wrappers and self-inclusion checks still narrow it until step 14):

* the TriggerMode of a registration becomes the multiplicity: "one or more" (ONCE_PER_BATCH, and the token slots'
  default) is TriggerOptions.ONE_OR_MORE, PER_TOKEN triggers for each token, and INDEPENDENT is simply an ability of
  its own;
* the landfall and enchanted-player registrations the old collector put on the stack as one object become one
  ability (MERGE_SLOTS), unless one of them was INDEPENDENT;
* MANUAL rewrites the few cards whose registration is a slot argument of an effect.

Usage: python3 scripts/step12a-enters-triggers.py DIR [DIR ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step12a = __import__("step12a-zone-change-triggers")
step11 = step12a.step11


def enters(filter_expr, relation, zone=None, options=None):
    trigger = "Triggers.enters(%s, ObjectRelation.{rel})" % filter_expr
    return (trigger, relation, zone) if options is None else (trigger, relation, zone, options)


def enters_from(zone_from, filter_expr, relation):
    return ("Triggers.entersFrom(Zone.%s, %s, ObjectRelation.{rel})" % (zone_from, filter_expr), relation, None)


FAMILY = {
    "ON_ALLY_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "ANOTHER_YOU_CONTROL"),
    "ON_ALLY_CREATURES_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "ANOTHER_YOU_CONTROL", None, "ONE_OR_MORE"),
    "ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "YOU_CONTROL"),
    "ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "YOU_CONTROL", None, "ONE_OR_MORE"),
    "ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.NONTOKEN_CREATURE", "ANOTHER_YOU_CONTROL"),
    "ON_SELF_OR_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.NONTOKEN_CREATURE", "YOU_CONTROL"),
    "COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.NONTOKEN_CREATURE", "YOU_CONTROL",
                                                                        "COMMAND"),
    "ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD": enters("Triggers.ARTIFACT", "ANOTHER_YOU_CONTROL"),
    "ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD": enters("Triggers.ARTIFACT", "YOU_CONTROL"),
    "ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD": enters("Triggers.NONTOKEN_ARTIFACT", "ANOTHER_YOU_CONTROL"),
    "ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD": enters("Triggers.NONTOKEN_ARTIFACT", "YOU_CONTROL"),
    "ON_ALLY_TOKEN_ENTERS_BATTLEFIELD": enters("Triggers.TOKEN", "YOU_CONTROL", None, "ONE_OR_MORE"),
    "ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD": enters("Triggers.TOKEN", "OPPONENT_CONTROLS", None, "ONE_OR_MORE"),
    "ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "ANOTHER"),
    "ON_ANY_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "ANY"),
    "ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "ENCHANTED_PLAYER_CONTROLS"),
    "ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD": enters("Triggers.LAND", "ENCHANTED_PLAYER_CONTROLS"),
    "ON_ALLY_PERMANENT_ENTERS_TRANSFORMED": enters("Triggers.TRANSFORMED", "YOU_CONTROL"),
    "ON_OPPONENT_LAND_ENTERS_BATTLEFIELD": enters("Triggers.LAND", "OPPONENT_CONTROLS"),
    "ON_ALLY_LAND_ENTERS_BATTLEFIELD": enters("Triggers.LAND", "YOU_CONTROL"),
    "ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD": enters("Triggers.EQUIPMENT", "ANOTHER_YOU_CONTROL"),
    "ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD": enters("Triggers.ENCHANTMENT", "ANOTHER_YOU_CONTROL"),
    "ON_SELF_OR_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD": enters("Triggers.ENCHANTMENT", "YOU_CONTROL"),
    "ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "OPPONENT_CONTROLS"),
    "ON_CREATURE_ENTERS_FROM_GRAVEYARD": enters_from("GRAVEYARD", "Triggers.CREATURE", "ANY"),
    "ON_PERMANENT_ENTERS_FROM_GRAVEYARD": enters_from("GRAVEYARD", "null", "ANY"),
    "ON_PERMANENT_ENTERS_FROM_EXILE": enters_from("EXILE", "null", "YOU_CONTROL"),
    "ON_SELF_ENTERS_FROM_GRAVEYARD": enters_from("GRAVEYARD", "null", "THIS"),
    "ON_ANY_PERMANENT_ENTERS_BATTLEFIELD": enters("null", "ANY"),
    "ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD": enters("null", "ANOTHER_YOU_CONTROL"),
    "ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE_OR_ENCHANTMENT", "YOU_CONTROL"),
    "GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD": enters("Triggers.LAND", "YOU_CONTROL", "GRAVEYARD"),
    "GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "YOU_CONTROL", "GRAVEYARD"),
    "GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD": enters("null", "ANY", "GRAVEYARD"),
    "GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD": enters("Triggers.CREATURE", "ANY", "GRAVEYARD"),
    "GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD": enters("Triggers.CREATURE", "ANOTHER",
                                                                                 "GRAVEYARD"),
    "GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD": enters("Triggers.ARTIFACT", "YOU_CONTROL", "GRAVEYARD"),
    "GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD": enters("Triggers.ENCHANTMENT", "YOU_CONTROL", "GRAVEYARD"),
}

step12a.TRIGGER_MODES.update({
    # Once for each token, not once for the batch.
    "PER_TOKEN": ("options", "DEFAULT"),
    # An ability of its own, which every registration now is.
    "INDEPENDENT": ("options", None),
})

MERGE_SLOTS = ("ON_ALLY_LAND_ENTERS_BATTLEFIELD", "ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD")

MANUAL = {
    # "Whenever you create one or more tokens for the first time each turn" isn't an enters trigger either.
    "AkimTheSoaringWind": [(
        """        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                OncePerTurnTriggerEffect.firstTokenCreation(""",
        """        addTrigger(new TriggerCondition(Triggers.tokensCreated(null, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                OncePerTurnTriggerEffect.firstTokenCreation(""")],
    # "Whenever you create one or more creature tokens" isn't an enters trigger (TriggerMode.TOKEN_CREATION).
    "StaffOfTheStoryteller": [(
        """        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new PutCountersOnSelfEffect(CounterType.STORY)),
                TriggerMode.TOKEN_CREATION);""",
        """        addTrigger(new TriggerCondition(Triggers.tokensCreated(null, ObjectRelation.YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new PutCountersOnSelfEffect(CounterType.STORY)));""")],
    "DownInTheValley": [(
        """new GrantEffectToTargetEffect(
                EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, ELF_TOKEN, EffectDuration.PERMANENT, false,
                GrantScope.SELF)""",
        """new GrantEffectToTargetEffect(
                new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), ELF_TOKEN),
                EffectDuration.PERMANENT, false, GrantScope.SELF)""")],
    # "Except it has this ability": the copy keeps the triggered ability that holds the copy effect.
    "ProteanThaumaturge": [(
        """new BecomeCopyOfTargetCreatureEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD)""",
        """BecomeCopyOfTargetCreatureEffect.retainingItsAbility()""")],
}


def merge(src, slot):
    """Joins consecutive registrations of `slot` (statements, or calls chained on one target group) that carry no
    TriggerMode into one registration of several effects, the one stack object the old collector made of them."""
    head = re.compile(r"(\.?)addEffect\(\s*EffectSlot\.%s\s*,\s*" % slot)
    while True:
        changed = False
        for m in head.finditer(src):
            close = step11.find_close(src, src.index("(", m.start()))
            rest = src[m.end():close]
            if re.search(r"TriggerMode\.\w+\s*$", rest):
                continue
            after = src[close + 1:]
            if m.group(1) == ".":
                nxt = re.match(r"\s*\.addEffect\(\s*EffectSlot\.%s\s*,\s*" % slot, after)
            else:
                nxt = re.match(r"\s*;\s*addEffect\(\s*EffectSlot\.%s\s*,\s*" % slot, after)
            if nxt is None:
                continue
            start2 = close + 1 + nxt.end()
            close2 = step11.find_close(src, src.index("(", close + 1 + nxt.start() + nxt.group(0).index("addEffect")))
            rest2 = src[start2:close2]
            if re.search(r"TriggerMode\.\w+\s*$", rest2):
                continue
            indent = " " * (len(src[:m.start()]) - src[:m.start()].rfind("\n") - 1 + 8)
            src = src[:close] + ",\n" + indent + rest2.strip() + src[close2:]
            changed = True
            break
        if not changed:
            return src


def main():
    slot_re = re.compile(r"EffectSlot\.(%s)\b" % "|".join(FAMILY))
    for root_dir in sys.argv[1:]:
        for root, _, files in os.walk(root_dir):
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if not slot_re.search(src):
                    continue
                card = name[:-5]
                for old, new in MANUAL.get(card, []):
                    if old not in src:
                        raise ValueError("%s: manual text not found" % card)
                    src = src.replace(old, new)
                for slot in MERGE_SLOTS:
                    src = merge(src, slot)
                src = step12a.convert_calls(src, card, FAMILY)
                src = step11.fix_imports(src)
                if slot_re.search(src):
                    raise ValueError("%s: a registration is left" % card)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
