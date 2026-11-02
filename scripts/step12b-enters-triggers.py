#!/usr/bin/env python3
"""Moves card classes and tests off the ON_ENTER_BATTLEFIELD EffectSlot (effect-slot-refactor.md step 12b).

Every registration becomes a triggered ability with the real trigger Triggers.enters(ObjectRelation.THIS), one
ability per registration, as the enters collector reads them:

* addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, e) -> addTrigger(Triggers.enters(ObjectRelation.THIS), e), on the card
  or chained on a target group;
* a TriggerMode.INDEPENDENT registration keeps its mode, addTrigger(trigger, TriggerMode.INDEPENDENT, e): the enters
  collector still puts the card's plain enters abilities on the stack as one object and an independent one as an
  object of its own; a TriggerMode.NORMAL is dropped;
* addSimultaneousEffects(EffectSlot.ON_ENTER_BATTLEFIELD, ...) -> addSimultaneousTrigger(trigger, ...);
* LegacySlots.triggered(EffectSlot.ON_ENTER_BATTLEFIELD, effects) -> new TriggeredAbility(trigger, effects...);
* the grant constructors that take the slot take the ability;
* getEffects / getEffectRegistrations of the slot, in tests, read the trigger.

MANUAL rewrites the cards whose registration passes the slot through a helper of their own.

Usage: python3 scripts/step12b-enters-triggers.py DIR [DIR ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-triggers")
readers = __import__("step12b-readers")

SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.ON_ENTER_BATTLEFIELD"
ENTERS = "Triggers.enters(ObjectRelation.THIS)"
GRANT_HEADS = ("new GrantTriggeredAbilityEffect", "new GrantTriggeredAbilityToCastSpellEffect")

MANUAL = {
    # The helper added the two effects to a slot; one ability with both effects is the stack object they made.
    "HuntmasterOfTheFells": [
        ("        addWolfAndLifeTrigger(EffectSlot.ON_ENTER_BATTLEFIELD);\n",
         "        addTrigger(%s, wolfToken(), new GainLifeEffect(2));\n" % ENTERS),
        ("""    private void addWolfAndLifeTrigger(EffectSlot slot) {
        addEffect(slot, wolfToken());
        addEffect(slot, new GainLifeEffect(2));
    }

""", ""),
    ],
    # The helper registered the same effect on the enters and the attack slot.
    "MirkwoodElk": [
        ("""        addTriggerEffects(EffectSlot.ON_ENTER_BATTLEFIELD);
        addTriggerEffects(EffectSlot.ON_ATTACK);
    }

    private void addTriggerEffects(EffectSlot slot) {
        addEffect(slot, SequenceEffect.of(""",
         """        addTrigger(%s, returnElfAndGainLife());
        addEffect(EffectSlot.ON_ATTACK, returnElfAndGainLife());
    }

    private static CardEffect returnElfAndGainLife() {
        return SequenceEffect.of(""" % ENTERS),
        ("""                new GainLifeEffect(new EventValue())));
    }""", """                new GainLifeEffect(new EventValue()));
    }"""),
        ("import com.github.laxika.magicalvibes.model.amount.EventValue;\n",
         "import com.github.laxika.magicalvibes.model.amount.EventValue;\n"
         "import com.github.laxika.magicalvibes.model.effect.CardEffect;\n"),
    ],
    # The grant's slot argument is inside the enters registration.
    "RopeLineAttendant": [
        ("""                new PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        List.of(new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN))),""",
         """                new PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect(
                        List.of(new TriggeredAbility(%s,
                                new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)))),""" % ENTERS),
    ],
}


def unwrap_list(effects):
    """The elements of a List.of(...) argument, or the argument itself."""
    effects = effects.strip()
    list_of = re.match(r"List\.(<\w+>)?of\(", effects)
    if list_of and step11.find_close(effects, list_of.end() - 1) == len(effects) - 1:
        return effects[list_of.end():-1]
    return effects


def convert_calls(src, card):
    call = re.compile(r"(addEffect|addSimultaneousEffects|LegacySlots\.triggered|%s)\((\s*)%s\s*,(\s*)"
                      % ("|".join(re.escape(h) for h in GRANT_HEADS), SLOT))
    # The last registration first, so one nested in another's arguments (a conjured card's enters ability) is
    # converted before the one around it.
    for m in reversed(list(call.finditer(src))):
        close = step11.find_close(src, src.index("(", m.start()))
        head, lead, gap = m.group(1), m.group(2), m.group(3)
        sep = "," + gap if "\n" in gap else ", "
        rest = src[m.end():close]
        mode = re.search(r",\s*TriggerMode\.(\w+)\s*$", rest)
        if mode:
            rest = rest[:mode.start()]
        if head == "addEffect":
            if mode is None or mode.group(1) == "NORMAL":
                body = "addTrigger(%s%s%s)" % (ENTERS, sep, rest)
            elif mode.group(1) == "INDEPENDENT":
                body = "addTrigger(%s, TriggerMode.INDEPENDENT%s%s)" % (ENTERS, sep, rest)
            else:
                raise ValueError("%s: an enters registration has TriggerMode.%s" % (card, mode.group(1)))
        elif mode is not None:
            raise ValueError("%s: a %s of the slot has a TriggerMode" % (card, head))
        elif head == "addSimultaneousEffects":
            body = "addSimultaneousTrigger(%s%s%s)" % (ENTERS, sep, rest)
        elif head == "LegacySlots.triggered":
            body = "new TriggeredAbility(%s%s%s)" % (ENTERS, sep, unwrap_list(rest))
        else:
            args = step11.split_args(rest)
            first = args[0]
            first_end = rest.index(first) + len(first)
            body = "%s(%snew TriggeredAbility(%s%s%s)%s)" % (head, lead, ENTERS, sep, first, rest[first_end:])
        src = src[:m.start()] + body + src[close + 1:]
    return src


def wrap_long_lines(src):
    """Breaks a converted line longer than 120 characters after its trigger (and mode) argument."""
    lines = []
    for line in src.split("\n"):
        m = re.match(r"^(\s*)(.*?(?:addTrigger|addSimultaneousTrigger|new TriggeredAbility)\(Triggers\.enters\("
                     r"ObjectRelation\.THIS\),(?: TriggerMode\.INDEPENDENT,)?) (.+)$", line)
        if len(line) > 120 and m:
            lines.append(m.group(1) + m.group(2))
            lines.append(m.group(1) + "        " + m.group(3))
        else:
            lines.append(line)
    return "\n".join(lines)


def drop_unused_imports(src):
    body = re.sub(r"^import .*$", "", src, flags=re.M)
    for fqcn in ("com.github.laxika.magicalvibes.model.LegacySlots",
                 "com.github.laxika.magicalvibes.model.TriggerMode"):
        simple = fqcn.rsplit(".", 1)[1]
        if not re.search(r"\b%s\b" % simple, body):
            src = re.sub(r"^import %s;\n" % re.escape(fqcn), "", src, flags=re.M)
    return src


def main():
    slot_re = re.compile(SLOT + r"\b")
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
                src = readers.convert(src)
                src = wrap_long_lines(src)
                if "new TriggeredAbility(" in src or "Triggers." in src:
                    src = step11.fix_imports(src)
                src = drop_unused_imports(src)
                if slot_re.search(re.sub(r"//.*|/\*[\s\S]*?\*/", "", src)):
                    raise ValueError("%s: a registration is left" % card)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
