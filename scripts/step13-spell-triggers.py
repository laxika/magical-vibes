#!/usr/bin/env python3
"""Moves card classes and tests off the spell, land-play and activation EffectSlots (effect-slot-refactor.md step 13,
family 2).

Every registration becomes a triggered ability with a real SpellTrigger, LandPlayTrigger or AbilityTrigger, one
ability per registration, as the old collectors read them. SLOTS maps each slot to its trigger and to the zone the
ability functions from (None for the trigger's default: the battlefield, or the stack for "this spell"):

* addEffect(EffectSlot.X, e) -> addTrigger(T, e), on the card or chained on a target group; from another zone
  addTrigger(new TriggerCondition(T, AbilityZones.Z), TriggerOptions.DEFAULT, e);
* a TriggerMode.INDEPENDENT registration keeps its mode, addTrigger(T, TriggerMode.INDEPENDENT, e);
* LegacySlots.triggered(EffectSlot.X, effects) -> new TriggeredAbility(T, effects...);
* the grant and registration constructors that take the slot and its effect take the ability: Head(EffectSlot.X, e,
  rest) -> Head(new TriggeredAbility(T, e), rest);
* the perpetual hand grants, the perpetual removal and the temporary copy's retained slot take the trigger;
* in tests, getEffects / getEffectRegistrations of a slot read the trigger, and perpetualAbilityEffects reads
  perpetualTriggerEffects.

MANUAL rewrites what doesn't fit those shapes.

Usage: python3 scripts/step13-spell-triggers.py DIR [DIR ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-triggers")

SLOTS = {
    "ON_SELF_MANA_ABILITY_RESOLVES": ("Triggers.thisManaAbilityResolves()", None),
    "ON_ANY_PLAYER_CASTS_SPELL": ("Triggers.playerCastsSpell()", None),
    "ON_CONTROLLER_CASTS_SPELL": ("Triggers.youCastSpell()", None),
    "ON_CONTROLLER_COPIES_SPELL": ("Triggers.youCopySpell()", None),
    "ON_OPPONENT_COPIES_SPELL": ("Triggers.opponentCopiesSpell()", None),
    "ON_CONTROLLER_COUNTERS_SPELL": ("Triggers.youCounterSpell()", None),
    "ON_CONTROLLER_SPELL_COUNTERED": ("Triggers.yourSpellCountered()", None),
    "ON_SELF_SPELL_COUNTERED_OR_FIZZLED": ("Triggers.thisSpellCounteredOrFizzles()", None),
    "ON_CONTROLLER_PLAYS_LAND": ("Triggers.youPlayLand()", None),
    "ON_CONTROLLER_PLAYS_ANOTHER_LAND": ("Triggers.youPlayAnotherLand()", None),
    "ON_OPPONENT_PLAYS_LAND": ("Triggers.opponentPlaysLand()", None),
    "ON_OPPONENT_CASTS_SPELL": ("Triggers.opponentCastsSpell()", None),
    "GRAVEYARD_ON_CONTROLLER_CASTS_SPELL": ("Triggers.youCastSpell()", "GRAVEYARD"),
    "HAND_ON_CONTROLLER_CASTS_SPELL": ("Triggers.youCastSpell()", "HAND"),
    "EXILE_ON_CONTROLLER_CASTS_SPELL": ("Triggers.youCastSpell()", "EXILE"),
    "GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY": ("Triggers.youActivateExhaustAbility()", "GRAVEYARD"),
    "COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL": ("Triggers.youCastSpell()", "COMMAND"),
    "ON_SELF_CAST": ("Triggers.youCastThisSpell()", None),
    "ON_CONTROLLER_ACTIVATES_ABILITY": ("Triggers.youActivateAbility()", None),
    "ON_OPPONENT_ACTIVATES_ABILITY": ("Triggers.opponentActivatesAbility()", None),
    "ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY": ("Triggers.youActivateExhaustAbility()", None),
    "ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY": ("Triggers.youActivateNonManaAbility()", None),
    "ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM": ("Triggers.youActivateEternalizeOrEmbalm()", None),
    "ON_OPPONENT_ACTIVATES_NONMANA_ABILITY": ("Triggers.opponentActivatesNonManaAbility()", None),
}

SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.(%s)\b" % "|".join(SLOTS)
GRANT_HEADS = ("new GrantTriggeredAbilityEffect", "new GrantEffectToTargetEffect", "GrantEffectToTargetEffect.toSourcePermanent",
               "new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect",
               "new GrantTriggeredAbilityToCreatedPermanentsEffect", "new GrantEffectToSourceEffect",
               "new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect",
               "new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect")
# Calls whose slot argument names the trigger of the ability they grant, keep or remove.
TRIGGER_ARG_HEADS = ("PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect",
                     "PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect",
                     "PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect",
                     "ChooseCardsFromHandToPerpetuallyGrantTriggeredAbilityEffect",
                     "RemovePerpetualTriggeredAbilityEffect", "BecomeCopyOfTargetCreatureUntilEndOfTurnEffect",
                     "getTriggerEffects")

MANUAL = {
    # The copy keeps the triggered ability that holds the copy effect, which has a real trigger now.
    "ArtisanOfForms": [
        ("new BecomeCopyOfTargetCreatureEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL)",
         "BecomeCopyOfTargetCreatureEffect.retainingItsAbility()"),
    ],
}


def trigger_of(slot):
    return SLOTS[slot][0]


def condition_of(slot):
    trigger, zone = SLOTS[slot]
    return "new TriggerCondition(%s, AbilityZones.%s)" % (trigger, zone)


def unwrap_list(effects):
    effects = effects.strip()
    list_of = re.match(r"List\.(<\w+>)?of\(", effects)
    if list_of and step11.find_close(effects, list_of.end() - 1) == len(effects) - 1:
        return effects[list_of.end():-1]
    return effects


def convert_calls(src, card):
    call = re.compile(r"(addEffect|addSimultaneousEffects|LegacySlots\.triggered|%s)\((\s*)%s\s*,(\s*)"
                      % ("|".join(re.escape(h) for h in GRANT_HEADS), SLOT))
    for m in reversed(list(call.finditer(src))):
        close = step11.find_close(src, src.index("(", m.start()))
        head, lead, slot, gap = m.group(1), m.group(2), m.group(3), m.group(4)
        zone = SLOTS[slot][1]
        trigger = trigger_of(slot)
        sep = "," + gap if "\n" in gap else ", "
        rest = src[m.end():close]
        mode = re.search(r",\s*TriggerMode\.(\w+)\s*$", rest)
        if mode and head == "addEffect":
            rest = rest[:mode.start()]
        if head == "addEffect":
            if mode is not None and mode.group(1) not in ("NORMAL", "INDEPENDENT"):
                raise ValueError("%s: a %s registration has TriggerMode.%s" % (card, slot, mode.group(1)))
            if zone is not None:
                if mode is not None and mode.group(1) == "INDEPENDENT":
                    raise ValueError("%s: an INDEPENDENT %s registration" % (card, slot))
                body = "addTrigger(%s, TriggerOptions.DEFAULT%s%s)" % (condition_of(slot), sep, rest)
            elif mode is not None and mode.group(1) == "INDEPENDENT":
                body = "addTrigger(%s, TriggerMode.INDEPENDENT%s%s)" % (trigger, sep, rest)
            else:
                body = "addTrigger(%s%s%s)" % (trigger, sep, rest)
        elif re.search(r"TriggerMode\.", rest) and head != "addEffect":
            raise ValueError("%s: a %s of %s has a TriggerMode" % (card, head, slot))
        elif head == "addSimultaneousEffects":
            if zone is not None:
                raise ValueError("%s: simultaneous effects in %s" % (card, slot))
            body = "addSimultaneousTrigger(%s%s%s)" % (trigger, sep, rest)
        elif head == "LegacySlots.triggered":
            first = trigger if zone is None else condition_of(slot)
            body = "new TriggeredAbility(%s%s%s)" % (first, sep, unwrap_list(rest))
        else:
            args = step11.split_args(rest)
            first = args[0]
            first_end = rest.index(first) + len(first)
            ability_head = trigger if zone is None else condition_of(slot)
            body = "%s(%snew TriggeredAbility(%s%s%s)%s)" % (head, lead, ability_head, sep, first, rest[first_end:])
        src = src[:m.start()] + body + src[close + 1:]
    return src


def convert_readers(src):
    """Test and helper readers: the slot names the trigger."""
    def zone_of(slot):
        return SLOTS[slot][1] or "BATTLEFIELD"

    def getter(name):
        def repl(m):
            slot = m.group(1)
            if SLOTS[slot][1] is None:
                return ".%s(%s)" % (name, trigger_of(slot))
            return ".%s(%s, Zone.%s)" % (name, trigger_of(slot), zone_of(slot))
        return repl

    src = re.sub(r"\.getEffects\(\s*%s\s*\)" % SLOT, getter("getTriggerEffects"), src)
    src = re.sub(r"\.getEffectRegistrations\(\s*%s\s*\)" % SLOT, getter("getTriggerEffectRegistrations"), src)
    src = re.sub(r"\.getPersistentTriggeredEffects\(\s*%s\s*\)" % SLOT,
                 lambda m: ".getPersistentTriggerEffects(%s)" % trigger_of(m.group(1)), src)
    src = re.sub(r"\.getTemporaryTriggeredEffects\(\s*%s\s*\)" % SLOT,
                 lambda m: ".getTemporaryTriggerEffects(%s)" % trigger_of(m.group(1)), src)
    for m in reversed(list(re.finditer(r"\.addPersistentTriggeredEffect\(\s*%s\s*,\s*" % SLOT, src))):
        close = step11.find_close(src, src.index("(", m.start()))
        src = src[:m.start()] + ".addPersistentAbility(new TriggeredAbility(%s, %s))" % (
            trigger_of(m.group(1)), src[m.end():close]) + src[close + 1:]
    src = re.sub(r"\.perpetualAbilityEffects\(([^;]*?),\s*%s\s*\)" % SLOT,
                 lambda m: ".perpetualTriggerEffects(%s, %s, Zone.%s)" % (
                     m.group(1), trigger_of(m.group(2)), "STACK" if m.group(2) == "ON_SELF_CAST" else zone_of(m.group(2))),
                 src)
    src = re.sub(r"\.grantedTriggeredEffects\(([^;()]*?),\s*%s\s*\)" % SLOT,
                 lambda m: ".grantedTriggerEffects(%s, %s)" % (m.group(1), trigger_of(m.group(2))), src)
    return src


def convert_trigger_args(src, card):
    """A slot left as an argument of a call that names a trigger with it."""
    out = []
    last = 0
    for m in re.finditer(SLOT, src):
        depth, i = 0, m.start() - 1
        while i >= 0:
            c = src[i]
            if c == ")":
                depth += 1
            elif c == "(":
                if depth == 0:
                    break
                depth -= 1
            i -= 1
        head = re.search(r"([\w.]+)\s*$", src[:i])
        if head is None or not head.group(1).endswith(TRIGGER_ARG_HEADS):
            raise ValueError("%s: %s left in %s" % (card, m.group(1), head.group(1) if head else "?"))
        if SLOTS[m.group(1)][1] is not None:
            raise ValueError("%s: %s outside the battlefield as an argument" % (card, m.group(1)))
        out.append(src[last:m.start()])
        out.append(trigger_of(m.group(1)))
        last = m.end()
    out.append(src[last:])
    return "".join(out)


def convert_comments(src):
    """A comment that names a slot names its trigger instead."""
    names = r"\b(%s)\b" % "|".join(SLOTS)
    return re.sub(r"//.*", lambda m: re.sub(names, lambda n: trigger_of(n.group(1)), m.group(0)), src)


def wrap_long_lines(src):
    lines = []
    for line in src.split("\n"):
        m = re.match(r"^(\s*)(.*?(?:addTrigger|addSimultaneousTrigger|new TriggeredAbility)\(Triggers\.\w+\(\),"
                     r"(?: TriggerMode\.INDEPENDENT,)?) (.+)$", line)
        zoned = re.match(r"^(\s*)(.*addTrigger\(new TriggerCondition\(.*?, AbilityZones\.\w+\),) (TriggerOptions\.DEFAULT,.*)$",
                         line)
        if len(line) > 120 and m:
            lines.append(m.group(1) + m.group(2))
            lines.append(m.group(1) + "        " + m.group(3))
        elif len(line) > 120 and zoned:
            lines.append(zoned.group(1) + zoned.group(2))
            lines.append(zoned.group(1) + "        " + zoned.group(3))
        else:
            lines.append(line)
    return "\n".join(lines)


def drop_unused_imports(src):
    body = re.sub(r"^import .*$", "", src, flags=re.M)
    for fqcn in ("com.github.laxika.magicalvibes.model.LegacySlots",
                 "com.github.laxika.magicalvibes.model.TriggerMode",
                 "com.github.laxika.magicalvibes.model.EffectSlot"):
        simple = fqcn.rsplit(".", 1)[1]
        if not re.search(r"\b%s\b" % simple, body):
            src = re.sub(r"^import %s;\n" % re.escape(fqcn), "", src, flags=re.M)
    return src


def main():
    if sys.argv[1] == "--rewrap":
        for path in sys.argv[2:]:
            src = open(path).read()
            out = wrap_long_lines(convert_comments(src))
            if out != src:
                open(path, "w").write(out)
                print("rewrapped:", path)
        return
    slot_re = re.compile(SLOT)
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
                src = convert_calls(src, card)
                src = convert_readers(src)
                src = convert_trigger_args(src, card)
                src = convert_comments(src)
                src = wrap_long_lines(src)
                if re.search(r"\bZone\.", src) and not re.search(r"^import com\.github\.laxika\.magicalvibes\.model\.Zone;$", src, re.M) \
                        and not path.endswith("/model/Zone.java"):
                    src = step11.add_import(src, "com.github.laxika.magicalvibes.model.Zone")
                src = step11.fix_imports(src)
                src = drop_unused_imports(src)
                if slot_re.search(re.sub(r"//.*|/\*[\s\S]*?\*/", "", src)):
                    raise ValueError("%s: a registration is left" % card)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
