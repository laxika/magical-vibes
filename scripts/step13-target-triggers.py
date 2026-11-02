#!/usr/bin/env python3
"""Moves card classes and tests off the becomes-the-target EffectSlots (effect-slot-refactor.md step 13, family 6).

Every registration becomes a triggered ability with a real TargetTrigger, one ability per registration, as the old
collectors read them. SLOTS, read from scripts/step13-target-slots.tsv, maps each slot to its trigger and to the zone
the ability functions from (None for the trigger's default, the battlefield):

* addEffect(EffectSlot.X, e) -> addTrigger(T, e), on the card or chained on a target group; from another zone
  addTrigger(new TriggerCondition(T, AbilityZones.Z), TriggerOptions.DEFAULT, e);
* a registration with a TriggerMode keeps it,
  addTrigger(T, TriggerMode.X, e);
* LegacySlots.triggered(EffectSlot.X, effects) -> new TriggeredAbility(T, effects...);
* the grant and registration constructors that take the slot and its effect take the ability: Head(EffectSlot.X, e,
  rest) -> Head(new TriggeredAbility(T, e), rest);
* the perpetual hand grant and removal and the copy's retained slots take the trigger;
* the perpetual keyword grants take the ability in place of the slot and its effect:
  Head(keywords, EffectSlot.X, e, rest) -> Head(keywords, new TriggeredAbility(T, e), rest);
* in tests, getEffects / getEffectRegistrations of a slot read the trigger, and perpetualAbilityEffects reads
  perpetualTriggerEffects.

MANUAL rewrites what doesn't fit those shapes.

Usage: python3 scripts/step13-target-triggers.py DIR [DIR ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-triggers")

ROWS = [line.rstrip("\n").split("\t") for line in open(os.path.join(os.path.dirname(__file__), "step13-target-slots.tsv"))
        if not line.startswith("#")]
SLOTS = {row[0]: ("Triggers.%s()" % row[1], None if row[2] == "-" else row[2]) for row in ROWS}

SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.(%s)\b" % "|".join(SLOTS)
GRANT_HEADS = ("new GrantTriggeredAbilityEffect", "new GrantEffectToTargetEffect",
               "GrantEffectToTargetEffect.toSourcePermanent", "GrantEffectToTargetEffect.toSource",
               "new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect",
               "new GrantTriggeredAbilityToCreatedPermanentsEffect", "new GrantEffectToSourceEffect",
               "new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect",
               "new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect",
               "new RegisterGlobalTriggeredAbilityUntilNextTurnEffect",
               "new GrantEffectToTargetUntilEndOfTurnEffect", "new GrantEffectToOwnCreaturesUntilEndOfTurnEffect",
               "new GrantEffectToSourceUntilEndOfTurnEffect",
               "new GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect",
               "new GrantTriggeredAbilityToCastSpellEffect", "new GrantEffectToTargetUntilEndOfCombatEffect",
               "new GrantEffectToChosenCreatureUntilEndOfTurnEffect",
               "new GrantEffectToOwnCreaturesUntilEndOfCombatEffect",
               "new PerpetuallyGrantTriggeredAbilityToSourceCardEffect",
               "GrantEffectToTargetEffect.toEnchantedPermanent")
# Calls whose slot argument names the trigger of the ability they grant, keep or remove.
TRIGGER_ARG_HEADS = ("ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect",
                     "RemovePerpetualTriggeredAbilityEffect", "getTriggerEffects")

# Calls that take the slot and its effect after their own leading arguments.
KEYWORD_GRANT_HEADS = ("PerpetuallyGrantKeywordsToTargetCreatureEffect", "PerpetuallyGrantKeywordToMatchingCardsEffect")

MANUAL = {
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
            if zone is not None:
                if mode is not None and mode.group(1) != "NORMAL":
                    raise ValueError("%s: a %s registration with TriggerMode.%s" % (card, slot, mode.group(1)))
                body = "addTrigger(%s, TriggerOptions.DEFAULT%s%s)" % (condition_of(slot), sep, rest)
            elif mode is not None and mode.group(1) != "NORMAL":
                body = "addTrigger(%s, TriggerMode.%s%s%s)" % (trigger, mode.group(1), sep, rest)
            else:
                body = "addTrigger(%s%s%s)" % (trigger, sep, rest)
        elif head == "addSimultaneousEffects" and re.match(r"\s*TriggerMode\.\w+\s*,", rest):
            if zone is not None:
                raise ValueError("%s: simultaneous effects in %s" % (card, slot))
            body = "addSimultaneousTrigger(%s%s%s)" % (trigger, sep, rest.lstrip())
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


def convert_keyword_grants(src, card):
    """A slot and its effect in the middle of a perpetual keyword grant become one ability."""
    for m in reversed(list(re.finditer(SLOT + r"\s*,(\s*)", src))):
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
        if head is None or not head.group(1).endswith(KEYWORD_GRANT_HEADS):
            continue
        close = step11.find_close(src, i)
        args = step11.split_args(src[m.end():close])
        effect = args[0]
        effect_end = src.index(effect, m.end()) + len(effect)
        gap = m.group(2)
        sep = "," + gap if "\n" in gap else ", "
        src = src[:m.start()] + "new TriggeredAbility(%s%s%s)" % (trigger_of(m.group(1)), sep, effect) + src[effect_end:]
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
                     m.group(1), trigger_of(m.group(2)), zone_of(m.group(2))),
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
                     r"(?: TriggerMode\.\w+,)?) (.+)$", line)
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
                 "com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger",
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
                src = convert_keyword_grants(src, card)
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
