#!/usr/bin/env python3
"""Step 14, run 3b of effect-slot-refactor.md: the trigger-only wrappers of step 12a part 2's "whenever [an object]
enters" abilities (an enters, entersFrom or tokensCreated trigger whose relation isn't THIS, collected by the entering
ZoneChangeWatch constants) become trigger filters, relations and TriggerOptions.

For an ability's one effect, outermost wrapper first, repeatedly:

- TriggeringCardConditionalEffect(c, x) and .gatingSource(c, x) -> the permanent form of the card predicate c as a
  filter, judged on the permanent as it entered (CR 603.6a).
- TriggeringPermanentConditionalEffect(p, x[, anyController]) -> filter p. On the any-permanent watch
  ("whenever a permanent enters", enters(null, ANY)) the two-argument form also meant "you control": relation
  YOU_CONTROL.
- EnteringCreatureMinPowerConditionalEffect(n, x) -> PermanentPowerAtLeastPredicate(n), MaxPower ->
  PermanentPowerAtMostPredicate, HasCounters(t, x) -> PermanentHasCountersPredicate(t), FromGraveyard / FromExile ->
  PermanentEnteredOrCastFromZonePredicate (FromExile also NONTOKEN), NotCast -> Not(PermanentWasCastPredicate),
  NotCastFromHand(x[, requiresCast]) -> NONTOKEN[, WasCast], Not(PermanentCastFromZonePredicate(HAND)); the land ones
  EnteringLandTappedConditionalEffect -> PermanentIsTappedPredicate, EnteringLandNotFromHandConditionalEffect ->
  Not(PermanentEnteredOrCastFromZonePredicate(HAND)).
- new OncePerTurnTriggerEffect(x) and .keyed(x, k) -> the limit ONCE_EACH_TURN; new OncePerTurnTriggerEffect(x, true)
  and .markOnAcceptance(x) -> DO_ONLY_ONCE_EACH_TURN (CR 603.2h); new OnceOnlyTriggerEffect(x) -> ONCE.
- new ConditionalEffect(c, x) / (c, x, true): an intervening "if" (CR 603.4) -> the option, except ControllerTurn
  ("during your turn"), which is the trigger's onlyDuringYourTurn(), and SourceCardInGraveyard, which stays an
  effect. ConditionalEffect.atTriggerTime(WasCast(true), x) ("if you cast it", checked only as it triggers) -> the
  filter PermanentCastBySourceControllerThisTurnPredicate; any other atTriggerTime condition -> the option.
  ConditionalEffect.unless(c, x) is no intervening "if" and stays.

"This creature or another [X] you control" on a self-or-ally creature watch: the old body let the source's own entry
through whatever its subtypes were (a non-gating TriggeringCardConditionalEffect), so the relation becomes
THIS_OR_ANOTHER_YOU_CONTROL when the card's Oracle text says "or another"; "a [X] you control" stays YOU_CONTROL, the
source then judged by the filter too (RELATIONS lists the cards whose relation a wording check can't decide).

A condition on the trigger condition's filter that is null becomes the filter; one on a filter X becomes
AllOf(X, ...). An effect passed as a local variable is followed to its one definition. Cards in SKIP keep their
wrapper; MANUAL is applied by exact replacement first. --dry-run prints the diffs, every relation decision and every
ability whose ZoneChangeWatch the conversion would change.

Usage: python3 scripts/step14-enters-options.py [--dry-run] <module dir>...
"""
import difflib
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)


def _load(name, file):
    spec = importlib.util.spec_from_file_location(name, os.path.join(_here, file))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


zo = _load("zone_change_options", "step14-zone-change-options.py")
watches = _load("zone_change_watches", "step14-zone-change-watches.py")
do = zo.do
po = zo.po
pilot = zo.scan.pilot

ABILITY = re.compile(r"\baddTrigger\(|new TriggeredAbility\(")
TRIGGER = zo.TRIGGER
ENTERING = {"enters", "entersFrom", "tokensCreated"}

SKIP = {
    # "If that creature is 1/1" is an intervening "if" the handler checks again on resolution through the wrapper;
    # no option says it yet.
    "SigilCaptain.java",
    # "The first time you create one or more tokens each turn" counts the tokens created before, which no limit says.
    "AkimTheSoaringWind.java",
    # Its own "when this enters from a graveyard" abilities (entersFrom(GRAVEYARD, THIS)) are run 4's.
    "PhyrexianDragonEngine.java",
    "ArchfiendsVessel.java",
    # Its limit is shared with its own enters abilities (run 4).
    "OasisOfRenewal.java",
    # "Whenever a land an opponent controls enters, if …": the intervening "if" is about the land's controller or the
    # land, which the opponent-land body binds and the trigger pipeline's context doesn't (step 15).
    "ArchaeomancersMap.java",
    "ConfoundingConundrum.java",
    "InvaderParasite.java",
    "TunnelIgnus.java",
    # Its "another land" ability shares its effect with its own "when this enters" copy, which run 4 merges.
    "FieldOfTheDead.java",
}

# Cards whose relation the Oracle wording check gets wrong, by file: the relation to use.
RELATIONS = {
    # "Whenever this creature or another creature you control enters, if that creature entered from a graveyard":
    # the "if" is about whichever creature entered, this one included.
    "BreathlessKnight.java": "YOU_CONTROL",
    # "Whenever Alex Wilder or another creature you control enters, if you cast it from anywhere other than your
    # hand": the same.
    "AlexWilderRunaway.java": "YOU_CONTROL",
}

MANUAL = {
    # Its "it becomes an artifact creature" animates the Vehicle that entered, which the handler told apart by the
    # wrapper; the effect says so now.
    "KolodinTriumphCaster.java": [(
        "GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null)",
        "GrantScope.TRIGGERING_PERMANENT, EffectDuration.UNTIL_END_OF_TURN, null)")],
}
MANUAL["CorruptionOfTowashi.java"] = [(
    """        OncePerTurnTriggerEffect drawTrigger = OncePerTurnTriggerEffect.markOnAcceptance(
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
        addTrigger(Triggers.transforms(null, ObjectRelation.YOU_CONTROL), drawTrigger);
        addTrigger(Triggers.enters(Triggers.TRANSFORMED, ObjectRelation.YOU_CONTROL), drawTrigger);""",
    """        // "Do this only once each turn" marks the source as the draw is accepted, which covers both trigger
        // conditions (CR 603.2h).
        TriggerOptions doOnlyOnce = TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN);
        MayEffect draw = new MayEffect(new DrawCardEffect(1), "Draw a card?");
        addTrigger(Triggers.transforms(null, ObjectRelation.YOU_CONTROL), doOnlyOnce, draw);
        addTrigger(Triggers.enters(Triggers.TRANSFORMED, ObjectRelation.YOU_CONTROL), doOnlyOnce, draw);""")]
# Cards whose wrapper comes from a helper method, which the conversion doesn't follow.
MANUAL["NaturesWrath.java"] = [
    ("""        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                sacrificeTrigger(CardSubtype.ISLAND, CardColor.BLUE));
        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                sacrificeTrigger(CardSubtype.SWAMP, CardColor.BLACK));""",
     """        addTrigger(Triggers.enters(landTypeOrColor(CardSubtype.ISLAND, CardColor.BLUE), ObjectRelation.ANY),
                sacrificeTrigger(CardSubtype.ISLAND, CardColor.BLUE));
        addTrigger(Triggers.enters(landTypeOrColor(CardSubtype.SWAMP, CardColor.BLACK), ObjectRelation.ANY),
                sacrificeTrigger(CardSubtype.SWAMP, CardColor.BLACK));"""),
    ("""    private static CardEffect sacrificeTrigger(CardSubtype landType, CardColor color) {
        return new TriggeringPermanentConditionalEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(landType),
                        new PermanentColorInPredicate(Set.of(color)))),
                new SacrificePermanentsEffect(
                        1,
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(landType),
                                new PermanentColorInPredicate(Set.of(color)))),
                        SacrificeRecipient.TARGET_PLAYER),
                true);
    }""",
     """    private static PermanentAnyOfPredicate landTypeOrColor(CardSubtype landType, CardColor color) {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(landType),
                new PermanentColorInPredicate(Set.of(color))));
    }

    private static CardEffect sacrificeTrigger(CardSubtype landType, CardColor color) {
        return new SacrificePermanentsEffect(1, landTypeOrColor(landType, color), SacrificeRecipient.TARGET_PLAYER);
    }"""),
    ("import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;\n", ""),
]
MANUAL["ExpeditionSupplier.java"] = [
    ("""        // "Whenever one or more Humans and/or Warriors you control enter" includes Expedition Supplier
        // itself. The batch slot only watches other creatures (and tokens), so the self-or-ally slot covers
        // the Supplier's own entry; the once-each-turn limit keeps the two from both producing a Knife.
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, conjureKnifeForHumanOrWarrior());
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), conjureKnifeForHumanOrWarrior());
    }

    private static TriggeringCardConditionalEffect conjureKnifeForHumanOrWarrior() {
        return new TriggeringCardConditionalEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.HUMAN),
                        new CardSubtypePredicate(CardSubtype.WARRIOR))),
                new OncePerTurnTriggerEffect(new ConjureCardToBattlefieldEffect("ZNR", "256")));
    }""",
     """        // "Whenever Expedition Supplier and/or one or more other Humans and/or Warriors you control enter":
        // one ability, which "triggers only once each turn".
        addTrigger(new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                                new PermanentAnyOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR))))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new ConjureCardToBattlefieldEffect("ZNR", "256"));
    }"""),
    ("""import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
""", """import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
"""),
    ("import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n",
     "import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n"
     "import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;\n"),
]
MANUAL_IMPORTS = {"CorruptionOfTowashi.java": ["TriggerOptions", "TriggerLimit"]}

FILTER = do.FILTER
IMPORTS = dict(zo.IMPORTS)
IMPORTS.update({name: FILTER + name for name in [
    "PermanentPowerAtLeastPredicate", "PermanentPowerAtMostPredicate", "PermanentHasCountersPredicate",
    "PermanentEnteredOrCastFromZonePredicate", "PermanentWasCastPredicate", "PermanentCastFromZonePredicate",
    "PermanentCastBySourceControllerThisTurnPredicate", "PermanentIsAuraPredicate", "PermanentIsColorlessPredicate",
    "PermanentToughnessGreaterThanPowerPredicate", "PermanentMaxManaValuePredicate", "PermanentIsSourceCardPredicate",
    "PermanentIsTappedPredicate"]})
IMPORTS["Zone"] = "com.github.laxika.magicalvibes.model.Zone"
IMPORTS["CounterType"] = "com.github.laxika.magicalvibes.model.CounterType"
IMPORTS["CardSubtype"] = "com.github.laxika.magicalvibes.model.CardSubtype"

WRAPPER_TYPES = zo.WRAPPER_TYPES | {"EnteringCreatureMinPowerConditionalEffect",
                                    "EnteringCreatureMaxPowerConditionalEffect",
                                    "EnteringCreatureHasCountersConditionalEffect",
                                    "EnteringCreatureFromGraveyardConditionalEffect",
                                    "EnteringCreatureFromExileConditionalEffect",
                                    "EnteringCreatureNotCastConditionalEffect",
                                    "EnteringCreatureNotCastFromHandConditionalEffect",
                                    "EnteringLandTappedConditionalEffect", "EnteringLandNotFromHandConditionalEffect"}

# The self-or-ally creature watches, whose old body let the source's own entry past a non-gating card filter.
SELF_OR_ALLY_CREATURE = {"ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD", "ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD"}

REPORT = []


def family_trigger(trigger):
    """(factory, leading args, filter, relation, suffix) of a 3b trigger expression, or None for another trigger."""
    m = TRIGGER.match(trigger.strip())
    if not m:
        return None
    name, args, suffix = m.group(1), po.split_args(m.group(2)), m.group(3)
    if name not in ENTERING or len(args) < 2:
        return None
    relation = args[-1].replace("ObjectRelation.", "")
    if relation == "THIS":
        return None
    return name, args[:-2], args[-2], args[-1], suffix


CARD_TO_PERMANENT = [
    ("new CardIsAuraPredicate", lambda a: "new PermanentHasSubtypePredicate(CardSubtype.AURA)"),
    ("new CardIsColorlessPredicate", lambda a: "new PermanentIsColorlessPredicate()"),
    ("new CardToughnessGreaterThanPowerPredicate", lambda a: "new PermanentToughnessGreaterThanPowerPredicate()"),
    ("new CardMaxManaValuePredicate", lambda a: "new PermanentMaxManaValuePredicate(%s)" % a[0]),
    ("new CardIsSelfPredicate", lambda a: "new PermanentIsSourceCardPredicate()"),
]
CARD_TYPES = dict(zo.CARD_TYPES)


def card_to_permanent(expr):
    flat = do.strip_fq(expr)
    for name, build in CARD_TO_PERMANENT:
        args = po.call(flat, name)
        if args is not None:
            return build(args)
    args = po.call(flat, "new CardTypePredicate")
    if args is not None and args[0] in CARD_TYPES:
        return CARD_TYPES[args[0]]
    return _zone_change_card_to_permanent(expr)


_zone_change_card_to_permanent = zo.card_to_permanent
zo.card_to_permanent = card_to_permanent
do.card_to_permanent = card_to_permanent

THIS_ALTERNATIVE = re.compile(r"^new PermanentAnyOfPredicate\(List\.of\(new (?:PermanentIsSourceCardPredicate|"
                              r"PermanentIsSourcePermanentPredicate)\(\),\s*(.*)\)\)$", re.S)


def without_this_alternative(flt):
    """{@code X} for "this or X", which THIS_OR_ANOTHER_YOU_CONTROL already says."""
    m = THIS_ALTERNATIVE.match(do.collapse(flt))
    if not m:
        return flt
    rest = po.split_args(m.group(1))
    return rest[0] if len(rest) == 1 else "new PermanentAnyOfPredicate(List.of(%s))" % ", ".join(rest)


class Unwrapped:
    def __init__(self):
        self.filters = []
        self.limit = None
        self.condition = None
        self.your_turn = False
        self.card_filter = False
        self.gating = False
        self.token_gate = False
        self.permanent_filter = False
        self.any_controller = None
        self.entering_filter = False
        self.effect = None


def unwrap(effect):
    """The outermost trigger-only wrappers of {@code effect}, taken off."""
    u = Unwrapped()

    def set_limit(value):
        if u.limit is not None:
            raise ValueError("two limits: " + effect)
        u.limit = value

    def set_condition(value):
        if do.strip_fq(value) == "new ControllerTurn()":
            u.your_turn = True
        elif u.condition is None:
            u.condition = value
        else:
            raise ValueError("two intervening ifs: " + effect)

    while True:
        flat = do.strip_fq(effect)
        args = po.call(flat, "new TriggeringCardConditionalEffect")
        gating = po.call(flat, "TriggeringCardConditionalEffect.gatingSource")
        if args is not None and len(args) in (2, 3) or gating is not None:
            args = args if args is not None else gating
            u.filters.append(do.card_to_permanent(args[0]))
            u.card_filter = True
            u.gating |= gating is not None or (len(args) == 3 and args[2] == "true")
            u.token_gate |= do.strip_fq(args[0]) == "new CardIsTokenPredicate()"
            effect = args[1]
            continue
        args = po.call(flat, "new TriggeringPermanentConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] in ("true", "false"))):
            if args[0] != "null":
                u.filters.append(args[0])
            u.permanent_filter = True
            any_controller = len(args) == 3 and args[2] == "true"
            if u.any_controller is not None and u.any_controller != any_controller:
                raise ValueError("two controller scopes: " + effect)
            u.any_controller = any_controller
            effect = args[1]
            continue
        entering = None
        for name, build in [
            ("new EnteringCreatureMinPowerConditionalEffect",
             lambda a: ["new PermanentPowerAtLeastPredicate(%s)" % a[0]]),
            ("new EnteringCreatureMaxPowerConditionalEffect",
             lambda a: ["new PermanentPowerAtMostPredicate(%s)" % a[0]]),
            ("new EnteringCreatureHasCountersConditionalEffect",
             lambda a: ["new PermanentHasCountersPredicate(%s)" % a[0]]),
            ("new EnteringCreatureFromGraveyardConditionalEffect",
             lambda a: ["new PermanentEnteredOrCastFromZonePredicate(Zone.GRAVEYARD)"]),
            ("new EnteringCreatureFromExileConditionalEffect",
             lambda a: ["Triggers.NONTOKEN", "new PermanentEnteredOrCastFromZonePredicate(Zone.EXILE)"]),
            ("new EnteringLandTappedConditionalEffect", lambda a: ["new PermanentIsTappedPredicate()"]),
            ("new EnteringLandNotFromHandConditionalEffect",
             lambda a: ["new PermanentNotPredicate(new PermanentEnteredOrCastFromZonePredicate(Zone.HAND))"]),
            ("new EnteringCreatureNotCastConditionalEffect",
             lambda a: ["new PermanentNotPredicate(new PermanentWasCastPredicate())"]),
            ("new EnteringCreatureNotCastFromHandConditionalEffect",
             lambda a: ["Triggers.NONTOKEN"] + (["new PermanentWasCastPredicate()"]
                                                if len(a) == 2 and a[1] == "true" else [])
                       + ["new PermanentNotPredicate(new PermanentCastFromZonePredicate(Zone.HAND))"]),
        ]:
            args = po.call(flat, name)
            if args is not None:
                entering = (build(args), args[-1] if "Power" in name or "Counters" in name else args[0])
                break
        if entering is not None:
            u.filters.extend(entering[0])
            u.entering_filter = True
            effect = entering[1]
            continue
        once = po.call(flat, "new OncePerTurnTriggerEffect")
        keyed = po.call(flat, "OncePerTurnTriggerEffect.keyed")
        accept = po.call(flat, "OncePerTurnTriggerEffect.markOnAcceptance")
        only = po.call(flat, "new OnceOnlyTriggerEffect")
        if once is not None and len(once) == 1:
            set_limit("ONCE_EACH_TURN")
            effect = once[0]
            continue
        if once is not None and len(once) == 2 and once[1] in ("true", "false"):
            set_limit("DO_ONLY_ONCE_EACH_TURN" if once[1] == "true" else "ONCE_EACH_TURN")
            effect = once[0]
            continue
        if keyed is not None and len(keyed) == 2:
            set_limit("ONCE_EACH_TURN")
            effect = keyed[0]
            continue
        if accept is not None and len(accept) == 1:
            set_limit("DO_ONLY_ONCE_EACH_TURN")
            effect = accept[0]
            continue
        if only is not None and len(only) == 1:
            set_limit("ONCE")
            effect = only[0]
            continue
        args = po.call(flat, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
            if do.strip_fq(args[0]).startswith("new SourceCardInGraveyard("):
                break
            set_condition(args[0])
            effect = args[1]
            continue
        args = po.call(flat, "ConditionalEffect.atTriggerTime")
        if args is not None:
            if do.strip_fq(args[0]) == "new WasCast(true)":
                u.filters.append("new PermanentCastBySourceControllerThisTurnPredicate()")
            else:
                set_condition(args[0])
            effect = args[1]
            continue
        if re.match(r"^(new )?(OncePerTurnTriggerEffect|OnceOnlyTriggerEffect|TriggeringPermanentConditional|"
                    r"TriggeringCardConditional|Entering(?:Creature|Land)\w+ConditionalEffect)", flat):
            raise ValueError("an unknown wrapper: " + effect)
        break
    u.effect = effect
    return u


def norm(text):
    """A trigger expression in the form step14-zone-change-watches.py keys watches by, with single-predicate filters
    named by their Triggers constant."""
    text = watches.norm(text)
    for constant, expr in [("CREATURE", "newPermanentIsCreaturePredicate()"),
                           ("ARTIFACT", "newPermanentIsArtifactPredicate()"),
                           ("ENCHANTMENT", "newPermanentIsEnchantmentPredicate()"),
                           ("LAND", "newPermanentIsLandPredicate()"),
                           ("TOKEN", "newPermanentIsTokenPredicate()"),
                           ("EQUIPMENT", "newPermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)")]:
        text = text.replace("(" + expr + ",", "(" + constant + ",")
    return text


WATCH_TABLE = None


def watch_of(trigger, zone, multi):
    """The ZoneChangeWatch whose key {@code trigger} is, as ZoneChangeWatch.of finds it."""
    global WATCH_TABLE
    if WATCH_TABLE is None:
        WATCH_TABLE = watches.keys()
    key = norm(trigger)
    key = re.sub(r"\.onlyDuringYourTurn\(\)$", "", key)
    found = WATCH_TABLE.get((key, zone, multi))
    if found:
        return found
    m = re.match(r"^(\w+)\((.*)\)$", key)
    if not m:
        return None
    args = po.split_args(m.group(2))
    flt = args[-2] if len(args) >= 2 else None
    lead = args[:-2]
    if flt and flt.startswith("newPermanentAllOfPredicate(List.of("):
        first = po.split_args(flt[len("newPermanentAllOfPredicate(List.of("):-2])[0]
        first = norm("x(" + first + ",y)")[2:-3]
        found = WATCH_TABLE.get(("%s(%s)" % (m.group(1), ",".join(lead + [first, args[-1]])), zone, multi))
        if found:
            return found
    return WATCH_TABLE.get(("%s(%s)" % (m.group(1), ",".join(lead + ["null", args[-1]])), zone, multi))


def says_or_another(oracle):
    return bool(re.search(r"\b(?:or another|and/or one or more other)\b[^.|]*\benters?\b", oracle or ""))


def build_trigger(parts, filters, your_turn, relation):
    name, leading, flt, rel, suffix = parts
    rel = relation or rel
    if filters:
        extra = filters[0] if len(filters) == 1 else "new PermanentAllOfPredicate(List.of(%s))" % ", ".join(filters)
        base = None if flt in (None, "null") else flt
        parts = po.call(do.collapse(extra), "new PermanentAllOfPredicate")
        parts = po.call(parts[0], "List.of") if parts is not None and len(parts) == 1 else None
        if base is not None and parts is not None:
            # AllOf(X, AllOf(a, b)) is AllOf(X, a, b).
            extra = ", ".join(parts)
        flt = extra if base is None else "new PermanentAllOfPredicate(List.of(%s, %s))" % (base, extra)
    if your_turn and ".onlyDuringYourTurn()" not in suffix:
        suffix += ".onlyDuringYourTurn()"
    return "Triggers.%s(%s)%s" % (name, ", ".join(leading + [flt or "null", rel]), suffix)


def zone_of(text):
    z = re.search(r"AbilityZones\.(\w+)", text)
    return z.group(1) if z else "BATTLEFIELD"


def convert(src, name):
    """Converts every 3b ability of the card; returns the new source and the names it needs."""
    needs = set()
    edits = []
    var_edits = {}
    spans = []
    oracle = pilot.oracle(pilot.REG.findall(src)) or ""
    for m in ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        condition_form = po.call(args[0], "new TriggerCondition")
        trigger = condition_form[0] if condition_form else args[0]
        parts = family_trigger(trigger)
        if parts is None:
            continue
        if condition_form is not None:
            existing, effects = args[1], args[2:]
        elif len(args) >= 3 and do.strip_fq(args[1]).startswith("TriggerOptions"):
            existing, effects = args[1], args[2:]
        else:
            existing, effects = None, args[1:]
        if len(effects) != 1:
            continue
        effect = effects[0]
        variable = None
        if re.fullmatch(r"[A-Za-z_]\w*", effect.strip()):
            found = do.definition(src, effect.strip())
            if found is None:
                continue
            variable = (effect.strip(), found)
            effect = found[2]
        u = unwrap(effect)
        if not (u.filters or u.limit or u.condition or u.your_turn or u.any_controller is not None):
            continue
        if m.group(0).startswith("new") and (u.limit or u.condition):
            raise ValueError("options on a granted ability: " + src[m.start():end])
        zone = zone_of(src[m.start():end])
        multi = "ONE_OR_MORE" if "ONE_OR_MORE" in (existing or "") else "EACH"
        old_watch = watch_of(trigger, zone, multi)
        relation = None
        current = parts[3].replace("ObjectRelation.", "")
        if old_watch == "ON_ANY_PERMANENT_ENTERS_BATTLEFIELD" and u.any_controller is False:
            relation = "YOU_CONTROL"
        if old_watch in SELF_OR_ALLY_CREATURE and current == "YOU_CONTROL" and u.filters:
            chosen = RELATIONS.get(name)
            if chosen is None:
                chosen = "THIS_OR_ANOTHER_YOU_CONTROL" if says_or_another(oracle) else "YOU_CONTROL"
            old_self = "exempt" if (u.card_filter and not u.gating and not u.token_gate
                                    and not u.permanent_filter and not u.entering_filter) else "gated"
            new_self = "exempt" if chosen == "THIS_OR_ANOTHER_YOU_CONTROL" else "gated"
            REPORT.append("RELATION %s %s (source %s -> %s)%s" % (name, chosen, old_self, new_self,
                                                                 "" if old_self == new_self else "  CHANGED"))
            if chosen != "YOU_CONTROL":
                relation = chosen
                u.filters = [without_this_alternative(f) for f in u.filters]
        new_trigger = build_trigger(parts, u.filters, u.your_turn,
                                    None if relation is None else "ObjectRelation." + relation)
        new_watch = watch_of(new_trigger, zone, multi)
        if new_watch != old_watch:
            REPORT.append("WATCH %s %s -> %s  %s" % (name, old_watch, new_watch, do.collapse(new_trigger)))
        if existing and not (u.limit or u.condition):
            new_options = existing
        elif existing or u.limit or u.condition:
            new_options = do.options_expr(existing, u.limit, u.condition)
        else:
            new_options = None
        if u.limit:
            needs.add("TriggerLimit")
        if new_options and "TriggerOptions" in new_options:
            needs.add("TriggerOptions")
        inner = u.effect
        if variable:
            vname, (vstart, vend, _) = variable
            if vname in var_edits and var_edits[vname][2] != inner:
                raise ValueError("a variable unwrapped two ways: " + vname)
            var_edits[vname] = (vstart, vend, inner)
            inner = vname
        if condition_form is not None:
            first = "new TriggerCondition(%s)" % ", ".join([new_trigger] + condition_form[1:])
        else:
            first = new_trigger
        line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(line) - len(line.lstrip()) + 8)
        body = do.layout([first] + ([new_options] if new_options else []) + [inner], indent,
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
    for vname in sorted(do.PREDICATE_VARIABLES):
        found = do.definition(src, vname)
        if found is None:
            raise ValueError("no definition of " + vname)
        vstart, vend, expr = found
        edits.append((vstart, vend, do.card_to_permanent(expr)))
        declared = re.search(r"(\b[A-Z]\w*|\bvar)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        edits.append((declared.start(1), declared.end(1), "var"))
    do.PREDICATE_VARIABLES.clear()
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    for imp in IMPORTS:
        if imp in ("List", "Set"):
            if re.search(r"\b%s\.of\(" % imp, src):
                needs.add(imp)
        elif imp in ("ObjectRelation", "Triggers", "Zone", "CounterType", "CardSubtype"):
            if re.search(r"\b%s\." % imp, src):
                needs.add(imp)
        elif re.search(r"\bnew %s\(" % imp, src):
            needs.add(imp)
    return src, needs


# Lines the conversion left past 120 columns, re-wrapped by exact replacement after it (each skipped once its new text
# is in the card). A pair is (old, new).
AFTER = {
    # "Whenever another Dwarf you control enters and whenever an Equipment you control enters, draw a card. This
    # ability triggers only once each turn": one ability of two trigger conditions, so the limit covers both.
    "KLiTheResourceful.java": [(
        """        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DWARF))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DrawCardEffect());
        addTrigger(Triggers.enters(Triggers.EQUIPMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DrawCardEffect());""",
        """        // "Whenever another Dwarf you control enters and whenever an Equipment you control enters, draw a card.
        // This ability triggers only once each turn": one ability of two trigger conditions under one limit.
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                        new PermanentHasSubtypePredicate(CardSubtype.DWARF))), ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.enters(Triggers.EQUIPMENT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                List.of(new EffectRegistration(new DrawCardEffect()))));"""),
        ("import com.github.laxika.magicalvibes.model.CardSubtype;\n",
         "import com.github.laxika.magicalvibes.model.CardSubtype;\n"
         "import com.github.laxika.magicalvibes.model.EffectRegistration;\n"
         "import com.github.laxika.magicalvibes.model.TriggeredAbility;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n",
         "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n")],
    # Comments that named the wrapper.
    "BindingMummy.java": [(
        "        // The subtype gate is applied by TriggeringCardConditionalEffect on the entering creature;",
        "        // The subtype gate is the trigger's filter, judged on the entering creature;")],
    "DireUndercurrents.java": [(
        "        // The color gate is applied by TriggeringCardConditionalEffect on the entering creature;",
        "        // The color gate is the trigger's filter, judged on the entering creature;")],
    # Converted in two passes (its wrapped effect is a constant, followed on the second): the option re-wrapped, the
    # Blood-token predicate shared.
    "VoldarenBloodcaster.java": [(
        """        // Blood tokens are artifacts — gated via TriggeringCardConditionalEffect on ally-artifact ETB.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ARTIFACT,
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(5, new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD)
                        )))),
                TRANSFORM_IF_FIVE_BLOOD);""",
        """        // Blood tokens are artifacts: the trigger's filter is "a Blood token" among them.
        var bloodToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(), new PermanentHasSubtypePredicate(CardSubtype.BLOOD)));
        addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.ARTIFACT, bloodToken)),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(5, bloodToken)),
                TRANSFORM_IF_FIVE_BLOOD);""")],
    "AprilONeilLiveOnTheScene.java": [(
        """        var mutantNinjaOrTurtle = new PermanentAnyOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.MUTANT), new PermanentHasSubtypePredicate(CardSubtype.NINJA), new PermanentHasSubtypePredicate(CardSubtype.TURTLE)));""",
        """        var mutantNinjaOrTurtle = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.MUTANT),
                new PermanentHasSubtypePredicate(CardSubtype.NINJA),
                new PermanentHasSubtypePredicate(CardSubtype.TURTLE)));""")],
    "MaralenFaeAscendant.java": [(
        """        var elfOrFaerie = new PermanentAnyOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.ELF), new PermanentHasSubtypePredicate(CardSubtype.FAERIE)));""",
        """        var elfOrFaerie = new PermanentAnyOfPredicate(List.of(new PermanentHasSubtypePredicate(CardSubtype.ELF),
                new PermanentHasSubtypePredicate(CardSubtype.FAERIE)));""")],
    "AwestruckCygnet.java": [(
        """                TriggerOptions.DEFAULT.withInterveningIf(new SourceMatchesPermanentPredicate(new PermanentNamedPredicate(CARD_NAME))),""",
        """                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceMatchesPermanentPredicate(new PermanentNamedPredicate(CARD_NAME))),""")],
    "BristlebaneBattler.java": [(
        """                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),""",
        """                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),""")],
    "ReluctantDounguard.java": [(
        """                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),""",
        """                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),""")],
    "EyeOfSingularity.java": [(
        """                        new PermanentNotPredicate(new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(), new PermanentHasSupertypePredicate(CardSupertype.BASIC)))),""",
        """                        new PermanentNotPredicate(new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.BASIC)))),""")],
    "TatyovaStewardOfTides.java": [(
        """                        TriggerOptions.DEFAULT.withInterveningIf(new ControlsPermanentCount(7, new PermanentIsLandPredicate())),""",
        """                        TriggerOptions.DEFAULT.withInterveningIf(
                                new ControlsPermanentCount(7, new PermanentIsLandPredicate())),""")],
}


def apply_after(module):
    for root, _, files in os.walk(module):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if name not in AFTER:
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            original = src
            for old, new in AFTER[name]:
                if new in src:
                    continue
                if old not in src:
                    # A card the conversion takes two passes over reaches its AFTER text on the second.
                    print("AFTER replacement not found yet in " + path)
                    continue
                src = src.replace(old, new)
            if src != original:
                open(path, "w").write(src)
                print(path)


def main():
    dry = "--dry-run" in sys.argv
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java") or name in SKIP:
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                original = src
                needs = set(MANUAL_IMPORTS.get(name, []))
                for old, new in MANUAL.get(name, []):
                    if (new and new in src) or (not new and old not in src):
                        continue
                    if old not in src:
                        raise SystemExit("MANUAL replacement not found in " + path)
                    src = src.replace(old, new)
                try:
                    src, found = convert(src, name)
                    needs |= found
                except ValueError as error:
                    do.PREDICATE_VARIABLES.clear()
                    if dry:
                        print("ERROR %s: %s" % (path, error))
                        continue
                    raise SystemExit("%s: %s" % (path, error))
                if src == original:
                    continue
                src = po.add_imports(src, [IMPORTS[n] for n in sorted(needs) if n in IMPORTS])
                src = po.drop_unused_imports(original, src)
                src = re.sub(r"(^import com\.[^\n]+;\n)(import java\.)", r"\1\n\2", src, flags=re.M)
                if dry:
                    print("=== " + path)
                    sys.stdout.writelines(difflib.unified_diff(original.splitlines(True), src.splitlines(True), n=0))
                else:
                    open(path, "w").write(src)
                    print(path)
        if not dry:
            apply_after(module)
    for line in REPORT:
        print(line)


if __name__ == "__main__":
    main()
