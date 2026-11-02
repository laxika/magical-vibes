#!/usr/bin/env python3
"""Step 14, run 2 of effect-slot-refactor.md: the trigger-only wrappers of step 11's creature-death abilities ("whenever
[another] creature dies", a dies trigger whose relation isn't THIS, collected through CreatureDeathWatch) become
trigger filters and TriggerOptions, and the "this creature or another [restricted] creature" cards that step 11 left
split become one ability.

For an ability's one effect, outermost wrapper first, repeatedly:

- TriggeringPermanentConditionalEffect(p, x) -> filter p, judged on the creature as it last existed (CR 603.10a).
- TriggeringCardConditionalEffect(c, x) -> the permanent form of the card predicate c as a filter.
- new OncePerTurnTriggerEffect(x) -> the limit ONCE_EACH_TURN ("this ability triggers only once each turn");
  OncePerTurnTriggerEffect.markOnAcceptance(x) -> DO_ONLY_ONCE_EACH_TURN ("do this only once each turn", CR 603.2h).
- new ConditionalEffect(c, x) / (c, x, true): an intervening "if" (CR 603.4) -> the option, except ControllerTurn
  ("dies during your turn"), which is the trigger's onlyDuringYourTurn().

An effect passed as a local variable is followed to its one definition, which is rewritten in place. "This creature or
another X" cards (MERGE) lose their "when this dies" ability: the other one becomes THIS_OR_ANOTHER[_YOU_CONTROL], whose
source matches whatever it was. Cards in MANUAL are rewritten by exact replacement, cards in SKIP keep their wrapper.

Usage: python3 scripts/step14-death-options.py <module dir>...
"""
import importlib.util
import os
import re
import sys

_spec = importlib.util.spec_from_file_location(
    "pilot_options", os.path.join(os.path.dirname(__file__), "step14-pilot-options.py"))
po = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(po)

ABILITY = re.compile(r"\baddTrigger\(|new TriggeredAbility\(")
DIES = re.compile(r"^Triggers\.(dies|creatureDies|nontokenCreatureDies)\((.*)\)((?:\.\w+\(\))*)$", re.S)
ZONE_CHANGE_RELATIONS = {"YOU_OWN", "OPPONENT_OWNS", "ENCHANTED", "ANOTHER_YOU_OWN", "THIS"}
ZONE_CHANGE_FILTERS = {"null", "Triggers.ARTIFACT", "Triggers.ENCHANTMENT", "Triggers.LAND", "Triggers.NONTOKEN",
                       "Triggers.AURA_OR_EQUIPMENT"}
FQ = "com.github.laxika.magicalvibes.model."

# Not trigger conditions, or not convertible in this run (effect-slot-refactor.md, step 14 run 2 findings).
SKIP = {
    # "return that card ... if it's a Samurai card": a condition on the card, not the creature that died.
    "OathkeeperTakenosDaisho.java",
    # "with greater mana value than this card": no permanent predicate compares with the source card yet.
    "KamiOfMourning.java",
    # A plane: collected at the choke point by its exact trigger, not by the pipeline.
    "Agyrem.java",
    # Its "you may" goes on the stack through the conditional's handler; without the wrapper the nontoken handler
    # would ask it before the ability is on the stack. Run 16 removes the may-unwrapping.
    "TheMasterGallifreysEnd.java",
}

# "This creature or another [restricted] creature": the "when this dies" half merges into the other one.
MERGE = {
    "MycoidShepherd", "CaldaiaGuardian", "HaviTheAllFather", "AzlaskTheSwellingScourge", "ShadowTheHedgehog",
    "JackdawSavior", "ArchghoulOfThraben", "HeadlessRider", "KalastriaHighborn", "MarshlandHordemaster",
    "MiaraThornOfTheGlade", "MidnightEntourage", "MillicentRestlessRevenant", "OmnathLocusOfRage",
    "OrahSkyclaveHierophant", "PashalikMons", "RundveltHordemaster", "ScionOfOpulence", "UndeadAugur",
    "XathridNecromancer", "DragonKamisEgg", "WeddingCrasher", "PolukranosEngineOfRuin", "TheGhoulGunslinger",
    "AnaxHardenedInTheForge", "NickValentinePrivateEye", "RotlungReanimator", "SkirkDrillSergeant", "VengefulDead",
    "DrossScorpion", "SusurianVoidborn", "AlBhedSalvagers",
}

JACKDAW_OLD = """        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING), returnCreature));
        addTrigger(Triggers.dies(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()"""
JACKDAW_NEW = """        // "Return another target creature card": its own half's filter leaves this card out, which covers both.
        addTrigger(Triggers.creatureDies(new PermanentHasKeywordPredicate(Keyword.FLYING),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                ReturnCardFromGraveyardEffect.builder()"""
GHOUL_OLD = """    private static final CardEffect ZOMBIE_OR_MUTANT_RAD_AND_TREASURE = new TriggeringCardConditionalEffect(
            new CardAnyOfPredicate(List.of(
                    new CardSubtypePredicate(CardSubtype.ZOMBIE),
                    new CardSubtypePredicate(CardSubtype.MUTANT))),
            RAD_AND_TREASURE);
"""
GHOUL_OLD2 = """        addTrigger(Triggers.dies(ObjectRelation.THIS), RAD_AND_TREASURE);
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                ZOMBIE_OR_MUTANT_RAD_AND_TREASURE);"""
GHOUL_NEW2 = """        addTrigger(Triggers.nontokenCreatureDies(new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                                new PermanentHasSubtypePredicate(CardSubtype.MUTANT))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                RAD_AND_TREASURE);"""
BOTH_COMMENT = """        // "If it had counters on it" is about whichever creature died, this one included: one ability with two trigger
        // conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
"""
ROLE_MODEL_OLD = """        deathTrigger.addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new MoveDyingSourceCountersToTargetCreatureEffect()));"""
ROLE_MODEL_NEW = BOTH_COMMENT + """        deathTrigger.addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new MoveDyingSourceCountersToTargetCreatureEffect()))));"""
HOST_OLD = """        deathTrigger.addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new MoveDyingSourceCountersToTargetCreatureEffect(true)));"""
HOST_NEW = BOTH_COMMENT + """        deathTrigger.addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new MoveDyingSourceCountersToTargetCreatureEffect(true)))));"""
SLURRK_OLD = """    private static final TriggeringPermanentConditionalEffect COUNTERED_CREATURE_DIES =
            new TriggeringPermanentConditionalEffect(
                    new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                    GROW_COUNTERED_CREATURES);
"""
SLURRK_OLD2 = """        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), COUNTERED_CREATURE_DIES);"""
SLURRK_NEW2 = """        // "If it had a +1/+1 counter on it" is about whichever creature died, this one included: one ability with two
        // trigger conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
""" + """        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(GROW_COUNTERED_CREATURES))));"""
BOTH_IMPORTS = ["TriggeredAbility", "TriggerCondition", "TriggerOptions", "EffectRegistration", "List"]
CASS_OLD = """        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL), deathEffect());
    }

    private CardEffect deathEffect() {
        return new TriggeringPermanentConditionalEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsEnchantedPredicate(),
                        new PermanentIsEquippedPredicate())),
                new ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect());
    }"""
CASS_NEW = """        // "If it was enchanted or equipped" is about whichever creature died, Cass included: one ability with two
        // trigger conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        var enchantedOrEquipped = new PermanentAnyOfPredicate(List.of(
                new PermanentIsEnchantedPredicate(),
                new PermanentIsEquippedPredicate()));
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(enchantedOrEquipped, ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                enchantedOrEquipped, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect()))));
    }"""
BASRI_OLD = """        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                createsKnightIfHadPlusOnePlusOneCounter());
    }

    private static TriggeringPermanentConditionalEffect createsKnightIfHadPlusOnePlusOneCounter() {
        return new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())
        );
    }"""
BASRI_NEW = """        // "If it had a +1/+1 counter on it" is about whichever creature died, this one included: one ability with two
        // trigger conditions and the same filter, not a "this or another" relation, whose source matches whatever it was.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.dies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE), ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.creatureDies(
                                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new CreateTokenEffect(1, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())))));
    }"""
# The merged Susurian ability takes over the target group its "when this dies" half was chained to.
AFTER = {
    "SusurianVoidborn.java": [("""                "Target must be an opponent"
        ));
        addTrigger(Triggers.creatureDies(""", """                "Target must be an opponent"
        )).addTrigger(Triggers.creatureDies(""")],
}
PELT_OLD = """        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), growWhenLarger);"""
PELT_NEW = """        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentPowerGreaterThanSourcePower()),
                new PutCountersOnSourceEffect(1, 1, 1));"""
MANUAL = {
    "CassHandOfVengeance.java": [(CASS_OLD, CASS_NEW)],
    "BasrisLieutenant.java": [(BASRI_OLD, BASRI_NEW)],
    "ReluctantRoleModel.java": [(ROLE_MODEL_OLD, ROLE_MODEL_NEW)],
    "HostOfTheHereafter.java": [(HOST_OLD, HOST_NEW)],
    "SlurrkAllIngesting.java": [(SLURRK_OLD, ""), (SLURRK_OLD2, SLURRK_NEW2)],
    "PeltCollector.java": [(PELT_OLD, PELT_NEW)],
    "JackdawSavior.java": [(JACKDAW_OLD, JACKDAW_NEW)],
    "TheGhoulGunslinger.java": [(GHOUL_OLD, ""), (GHOUL_OLD2, GHOUL_NEW2)],
}
MANUAL_IMPORTS = {
    "CassHandOfVengeance.java": BOTH_IMPORTS,
    "BasrisLieutenant.java": BOTH_IMPORTS,
    "ReluctantRoleModel.java": BOTH_IMPORTS,
    "HostOfTheHereafter.java": BOTH_IMPORTS,
    "SlurrkAllIngesting.java": BOTH_IMPORTS,
    "PeltCollector.java": ["TriggerOptions"],
    "TheGhoulGunslinger.java": ["PermanentAnyOfPredicate", "PermanentHasSubtypePredicate", "List"],
}

WRAPPER_TYPES = {"TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect", "OncePerTurnTriggerEffect",
                 "ConditionalEffect"}
FILTER = "com.github.laxika.magicalvibes.model.filter."
IMPORTS = {name: FILTER + name for name in [
    "PermanentAllOfPredicate", "PermanentAnyOfPredicate", "PermanentNotPredicate", "PermanentHasSubtypePredicate",
    "PermanentIsTokenPredicate", "PermanentColorInPredicate", "PermanentHasSupertypePredicate",
    "PermanentIsArtifactPredicate", "PermanentIsCreaturePredicate", "PermanentIsMulticoloredPredicate",
    "PermanentHasKeywordPredicate", "PermanentMinManaValuePredicate", "PermanentHasSourceChosenSubtypePredicate"]}
IMPORTS.update({
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "TriggerLimit": "com.github.laxika.magicalvibes.model.trigger.TriggerLimit",
    "List": "java.util.List",
    "Set": "java.util.Set",
    "CardEffect": "com.github.laxika.magicalvibes.model.effect.CardEffect",
    "TriggeredAbility": "com.github.laxika.magicalvibes.model.TriggeredAbility",
    "EffectRegistration": "com.github.laxika.magicalvibes.model.EffectRegistration",
    "TriggerCondition": "com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
    "TriggeringPermanentPowerGreaterThanSourcePower":
        "com.github.laxika.magicalvibes.model.condition.TriggeringPermanentPowerGreaterThanSourcePower",
})


def strip_fq(expr):
    return re.sub(re.escape(FQ) + r"[a-z]+\.", "", expr.strip())


PREDICATE_VARIABLES = set()


def card_to_permanent(expr):
    """The permanent predicate that asks of the creature as it last existed what the card predicate asks. A local
    variable holding a card predicate is kept, and its definition converted (PREDICATE_VARIABLES)."""
    expr = strip_fq(expr)
    if re.fullmatch(r"[a-z]\w*", expr):
        PREDICATE_VARIABLES.add(expr)
        return expr
    for name, build in [
        ("new CardSubtypePredicate", lambda a: "new PermanentHasSubtypePredicate(%s)" % a[0]),
        ("new CardIsTokenPredicate", lambda a: "new PermanentIsTokenPredicate()"),
        ("new CardNotPredicate", lambda a: "new PermanentNotPredicate(%s)" % card_to_permanent(a[0])),
        ("new CardColorPredicate", lambda a: "new PermanentColorInPredicate(Set.of(%s))" % a[0]),
        ("new CardSupertypePredicate", lambda a: "new PermanentHasSupertypePredicate(%s)" % a[0]),
        ("new CardIsMulticoloredPredicate", lambda a: "new PermanentIsMulticoloredPredicate()"),
        ("new CardKeywordPredicate", lambda a: "new PermanentHasKeywordPredicate(%s)" % a[0]),
        ("new CardMinManaValuePredicate", lambda a: "new PermanentMinManaValuePredicate(%s)" % a[0]),
        ("new CardHasSourceChosenSubtypePredicate", lambda a: "new PermanentHasSourceChosenSubtypePredicate()"),
    ]:
        args = po.call(expr, name)
        if args is not None:
            return build(args)
    args = po.call(expr, "new CardTypePredicate")
    if args is not None:
        kind = {"CardType.ARTIFACT": "Artifact", "CardType.CREATURE": "Creature"}.get(args[0])
        if kind is None:
            raise ValueError("unknown card type: " + expr)
        return "new PermanentIs%sPredicate()" % kind
    for name, perm in (("new CardAllOfPredicate", "PermanentAllOfPredicate"),
                       ("new CardAnyOfPredicate", "PermanentAnyOfPredicate")):
        args = po.call(expr, name)
        if args is not None:
            parts = po.call(args[0], "List.of")
            if parts is None:
                raise ValueError("unknown card predicate list: " + expr)
            return "new %s(List.of(%s))" % (perm, ", ".join(card_to_permanent(p) for p in parts))
    raise ValueError("unknown card predicate: " + expr)


def unwrap(effect):
    """(filters, limit, condition, during your turn, effect) with the outermost trigger-only wrappers taken off."""
    filters, limit, condition, your_turn = [], None, None, False
    while True:
        args = po.call(effect, "new TriggeringPermanentConditionalEffect")
        if args is not None and len(args) == 2:
            filters.append(args[0])
            effect = args[1]
            continue
        args = po.call(effect, "new TriggeringCardConditionalEffect")
        if args is not None and len(args) == 2:
            filters.append(card_to_permanent(args[0]))
            effect = args[1]
            continue
        once = po.call(effect, "new OncePerTurnTriggerEffect")
        accept = po.call(effect, "OncePerTurnTriggerEffect.markOnAcceptance")
        if (once is not None and len(once) == 1) or accept is not None:
            if limit is not None:
                raise ValueError("two limits: " + effect)
            limit = "ONCE_EACH_TURN" if once is not None else "DO_ONLY_ONCE_EACH_TURN"
            effect = (once or accept)[0]
            continue
        args = po.call(effect, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
            if strip_fq(args[0]).startswith("new SourceCardInGraveyard("):
                # "While this card is in your graveyard": no intervening "if" (run 1 keeps it in the effect).
                break
            if strip_fq(args[0]) == "new ControllerTurn()":
                your_turn = True
            elif condition is None:
                condition = args[0]
            else:
                raise ValueError("two intervening ifs: " + effect)
            effect = args[1]
            continue
        if re.match(r"^(new )?(OncePerTurnTriggerEffect|OnceOnlyTriggerEffect|TriggeringPermanentControllerConditional)",
                    strip_fq(effect)):
            raise ValueError("an unknown wrapper: " + effect)
        break
    return filters, limit, condition, your_turn, effect


def family_trigger(trigger):
    """The parts of a creature-death trigger expression, or None for another trigger."""
    m = DIES.match(trigger.strip())
    if not m:
        return None
    args = po.split_args(m.group(2))
    relation = args[-1].replace("ObjectRelation.", "")
    if relation in ZONE_CHANGE_RELATIONS:
        return None
    if m.group(1) == "dies" and (len(args) == 1 or args[0] in ZONE_CHANGE_FILTERS):
        return None
    return m.group(1), args[:-1], args[-1], m.group(3)


def with_filter(trigger, filters, your_turn, relation=None):
    kind, base, rel, suffix = family_trigger(trigger)
    rel = relation or rel
    if filters:
        flt = filters[0] if len(filters) == 1 else "new PermanentAllOfPredicate(List.of(%s))" % ", ".join(filters)
        if kind == "dies":
            flt = "new PermanentAllOfPredicate(List.of(%s, %s))" % (base[0], flt)
            base = []
        elif base:
            flt = "new PermanentAllOfPredicate(List.of(%s, %s))" % (base[0], flt)
            base = []
        base = [flt]
    if your_turn and ".onlyDuringYourTurn()" not in suffix:
        suffix += ".onlyDuringYourTurn()"
    return "Triggers.%s(%s)%s" % (kind, ", ".join(base + [rel]), suffix)


def options_expr(existing, limit, condition):
    opts = existing or "TriggerOptions.DEFAULT"
    if limit:
        opts += ".withLimit(TriggerLimit.%s)" % limit
    if condition:
        opts += ".withInterveningIf(%s)" % condition
    return opts


def collapse(text):
    return re.sub(r"\(\s+", "(", re.sub(r"\s+\)", ")", re.sub(r"\s+", " ", text.strip())))


def reindent(text, indent):
    """A multi-line expression with its continuation lines moved under {@code indent}, keeping their shape."""
    lines = text.strip().split("\n")
    rest = [l for l in lines[1:] if l.strip()]
    if not rest:
        return lines[0]
    least = min(len(l) - len(l.lstrip()) for l in rest)
    shifted = [(" " * (len(indent) + 8) + l[least:]) if l.strip() else "" for l in lines[1:]]
    return "\n".join([lines[0].rstrip()] + shifted)


def pretty(expr, indent, column):
    """{@code expr} starting at {@code column}: on one line when it fits in 120 columns, otherwise a predicate list
    or a trigger with one part per line under {@code indent}, or its original line breaks."""
    flat = collapse(expr)
    if column + len(flat) <= 118 and "\n" not in flat:
        return flat
    inner = indent + " " * 8
    for name in ("new PermanentAllOfPredicate", "new PermanentAnyOfPredicate"):
        args = po.call(flat, name)
        if args is not None and len(args) == 1 and po.call(args[0], "List.of") is not None:
            parts = po.call(args[0], "List.of")
            return "%s(List.of(\n%s%s))" % (name, inner, (",\n" + inner).join(
                pretty(part, inner, len(inner)) for part in parts))
    trigger = DIES.match(flat)
    if trigger:
        parts = po.split_args(trigger.group(2))
        return "Triggers.%s(\n%s%s)%s" % (trigger.group(1), inner, (",\n" + inner).join(
            pretty(part, inner, len(inner)) for part in parts), trigger.group(3))
    condition = po.call(flat, "new TriggerCondition")
    if condition is not None:
        return "new TriggerCondition(%s)" % layout(condition, inner, column + len("new TriggerCondition("))
    return reindent(expr, indent)


def layout(args, indent, start):
    """The arguments of a call that opens at column {@code start}: on one line when they fit, otherwise one per
    line under {@code indent}, each laid out by {@link pretty}."""
    one = ", ".join(collapse(a) for a in args)
    if start + len(one) + 2 <= 120 and "\n" not in one:
        return one
    out = [pretty(args[0], indent, start)]
    out += [pretty(arg, indent, len(indent)) for arg in args[1:]]
    return (",\n" + indent).join(out)


def definition(src, name):
    """The (start, end, expression) of the one local or field definition of `name`, or None."""
    found = list(re.finditer(r"(?:^[ \t]*|[;{}]\s*)(?:(?:private|public|static|final)\s+)*(?:var|[A-Z]\w*(?:<[^;=]*>)?)\s+"
                             + re.escape(name) + r"\s*=\s*", src, re.M))
    if len(found) != 1:
        return None
    start = found[0].end()
    i, depth = start, 0
    while True:
        c = src[i]
        if c == '"':
            i = re.compile(r'(?<!\\)"').search(src, i + 1).start()
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == ";" and depth == 0:
            break
        i += 1
    return start, i, src[start:i]


def convert(src, name):
    """Converts every creature-death ability of the card; returns the new source and the names it needs."""
    needs = set()
    edits = []  # (start, end, text), applied from the end
    var_edits = {}
    spans = []
    for m in ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        condition_form = po.call(args[0], "new TriggerCondition")
        trigger = condition_form[0] if condition_form else args[0]
        if family_trigger(trigger) is None:
            continue
        if condition_form is not None:
            existing, effects = args[1], args[2:]
        elif len(args) >= 3 and strip_fq(args[1]).startswith("TriggerOptions"):
            existing, effects = args[1], args[2:]
        else:
            existing, effects = None, args[1:]
        if len(effects) != 1:
            continue
        effect = effects[0]
        variable = None
        if re.fullmatch(r"[A-Za-z_]\w*", effect.strip()):
            found = definition(src, effect.strip())
            if found is None:
                continue
            variable = (effect.strip(), found)
            effect = found[2]
        filters, limit, condition, your_turn, inner = unwrap(effect)
        if not filters and not limit and not condition and not your_turn:
            continue
        if m.group(0).startswith("new") and (limit or condition):
            raise ValueError("options on a granted ability: " + src[m.start():end])
        new_trigger = with_filter(trigger, filters, your_turn)
        new_options = options_expr(existing, limit, condition) if (existing or limit or condition) else None
        if limit:
            needs.add("TriggerLimit")
        if new_options and "TriggerOptions" in new_options:
            needs.add("TriggerOptions")
        if variable:
            vname, (vstart, vend, _) = variable
            if vname in var_edits and var_edits[vname] != inner:
                raise ValueError("a variable unwrapped two ways: " + vname)
            var_edits[vname] = (vstart, vend, inner)
            inner = vname
        if condition_form is not None:
            first = "new TriggerCondition(%s)" % ", ".join([new_trigger] + condition_form[1:])
        else:
            first = new_trigger
        line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(line) - len(line.lstrip()) + 8)
        body = layout([first] + ([new_options] if new_options else []) + [inner], indent,
                      len(line) + len(m.group(0)))
        edits.append((m.start(), end, m.group(0) + body + ")"))
        spans.append((m.start(), end))
    for vname, (vstart, vend, _) in var_edits.items():
        for use in re.finditer(r"\b%s\b" % re.escape(vname), src):
            if vstart <= use.start() < vend or re.match(r"\s*=", src[use.end():]):
                continue
            if not any(a <= use.start() < b for a, b in spans):
                raise ValueError("%s is also used outside the converted abilities" % vname)
    for vname, (vstart, vend, text) in var_edits.items():
        edits.append((vstart, vend, text))
        declared = re.search(r"\b([A-Z]\w*)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        if declared and declared.group(1) in WRAPPER_TYPES:
            edits.append((declared.start(1), declared.end(1), "CardEffect"))
            needs.add("CardEffect")
    for vname in sorted(PREDICATE_VARIABLES):
        found = definition(src, vname)
        if found is None:
            raise ValueError("no definition of " + vname)
        vstart, vend, expr = found
        edits.append((vstart, vend, card_to_permanent(expr)))
        declared = re.search(r"(\b[A-Z]\w*|\bvar)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        edits.append((declared.start(1), declared.end(1), "var"))
    PREDICATE_VARIABLES.clear()
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    for imp in IMPORTS:
        if imp in ("List", "Set"):
            if re.search(r"\b%s\.of\(" % imp, src):
                needs.add(imp)
        elif re.search(r"\bnew %s\(" % imp, src):
            needs.add(imp)
    return src, needs


THIS_DIES = re.compile(r"addTrigger\(Triggers\.dies\(ObjectRelation\.THIS\),\s*")


def same(a, b):
    return [re.sub(r"\s+", "", e) for e in a] == [re.sub(r"\s+", "", e) for e in b]


def merge(src, name):
    """Drops a "this or another" card's "when this dies" ability and makes its other one include the source."""
    candidates = []
    for m in ABILITY.finditer(src):
        cend = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():cend - 1])
        parts = family_trigger(args[0])
        if parts and parts[2].replace("ObjectRelation.", "") in ("ANOTHER_YOU_CONTROL", "ANOTHER"):
            candidates.append((m, cend, args))
    if not candidates and re.search(r"ObjectRelation\.THIS_OR_ANOTHER", src):
        return src
    if len(candidates) != 1:
        raise ValueError("expected one 'another' ability, found %d" % len(candidates))
    m, cend, args = candidates[0]
    halves = []
    for t in THIS_DIES.finditer(src):
        tend = po.close_paren(src, t.start() + len("addTrigger("))
        targs = po.split_args(src[t.start() + len("addTrigger("):tend - 1])
        if same(targs[1:], args[1:]):
            halves.append((t.start(), tend))
    if len(halves) != 1:
        raise ValueError("expected one matching 'when this dies' ability, found %d" % len(halves))
    hstart, hend = halves[0]
    before = src[:hstart].rstrip()
    if before.endswith(".") and re.search(r"\)\s*\.$", before):
        # A link of a target group's chain: drop it and the dot before it.
        dot = before.rfind(".")
        cut = (len(src[:dot].rstrip()), hend)
    else:
        line = src.rfind("\n", 0, hstart)
        semi = src.index(";", hend)
        cut = (line, semi + 1)
    rel = "ObjectRelation.THIS_OR_" + args[0].rsplit("ObjectRelation.", 1)[1].split(")")[0]
    new_trigger = with_filter(args[0], [], False, relation=rel)
    line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
    indent = " " * (len(line) - len(line.lstrip()) + 8)
    text = m.group(0) + layout([new_trigger] + args[1:], indent, len(line) + len(m.group(0))) + ")"
    edits = sorted([(m.start(), cend, text), (cut[0], cut[1], "")], reverse=True)
    for a, b, t in edits:
        src = src[:a] + t + src[b:]
    return src


def main():
    for module in sys.argv[1:]:
        for root, _, files in os.walk(module):
            if "/build/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java") or name in SKIP:
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                original = src
                needs = set(MANUAL_IMPORTS.get(name, []))
                for old, new in MANUAL.get(name, []):
                    if new and new in src:
                        continue
                    if old not in src and not new:
                        continue
                    if old not in src:
                        raise SystemExit("MANUAL replacement not found in " + path)
                    src = src.replace(old, new)
                try:
                    src, found = convert(src, name)
                    needs |= found
                    if name[:-5] in MERGE and name not in MANUAL:
                        src = merge(src, name)
                except ValueError as error:
                    raise SystemExit("%s: %s" % (path, error))
                for old, new in AFTER.get(name, []):
                    if new not in src:
                        if old not in src:
                            raise SystemExit("AFTER replacement not found in " + path)
                        src = src.replace(old, new)
                if src == original:
                    continue
                src = po.add_imports(src, [IMPORTS[n] for n in sorted(needs)])
                src = po.drop_unused_imports(original, src)
                open(path, "w").write(src)
                print(path)


if __name__ == "__main__":
    main()
