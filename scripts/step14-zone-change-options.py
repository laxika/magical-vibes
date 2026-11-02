#!/usr/bin/env python3
"""Step 14, run 3a of effect-slot-refactor.md: the trigger-only wrappers of step 12a's zone-change and permanent-change
abilities (those ZoneChangeWatch collects, except the "whenever [an object] enters" watches, which are run 3b) become
trigger filters, relations and TriggerOptions.

For an ability's one effect, outermost wrapper first, repeatedly:

- TriggeringPermanentConditionalEffect(p, x[, anyController]) -> filter p, judged on the object as it last existed
  (CR 603.10a) or as it is after the change.
- TriggeringCardConditionalEffect(c, x) -> the permanent form of the card predicate c as a filter.
- EnchantedPermanentLeavesConditionalEffect(c, List.of(x...)) -> filter c, the effects x.
- TriggeringPermanentControllerConditionalEffect(x) / .another(x) -> relation YOU_CONTROL / ANOTHER_YOU_CONTROL in
  place of ANY.
- TriggeringArtifactControllerConditionalEffect(x[, false, nontoken]) / .another(x) -> relation YOU_CONTROL /
  ANOTHER_YOU_CONTROL in place of ANY, and the NONTOKEN filter for "a nontoken artifact".
- ConditionalTriggeringPermanentEffect(c, p, x) -> filter p and condition c.
- new OncePerTurnTriggerEffect(x) and OncePerTurnTriggerEffect.keyed(x, k) -> the limit ONCE_EACH_TURN;
  OncePerTurnTriggerEffect.markOnAcceptance(x) -> DO_ONLY_ONCE_EACH_TURN (CR 603.2h); new OnceOnlyTriggerEffect(x)
  -> ONCE.
- new ConditionalEffect(c, x) / (c, x, true): an intervening "if" (CR 603.4) -> the option, except ControllerTurn
  ("during your turn"), which is the trigger's onlyDuringYourTurn(), and SourceCardInGraveyard, which stays an effect
  (run 1). ConditionalEffect.unless(c, x) is no intervening "if" and stays.

A condition on the trigger condition's filter that is null becomes the filter; one on a filter X becomes
AllOf(X, ...). An effect passed as a local variable is followed to its one definition. Cards in SKIP keep their
wrapper; MANUAL is applied by exact replacement first.

Usage: python3 scripts/step14-zone-change-options.py [--dry-run] <module dir>...
"""
import importlib.util
import os
import re
import sys

_spec = importlib.util.spec_from_file_location(
    "death_options", os.path.join(os.path.dirname(__file__), "step14-death-options.py"))
do = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(do)
po = do.po

_scan = importlib.util.spec_from_file_location(
    "zone_change_scan", os.path.join(os.path.dirname(__file__), "step14-zone-change-scan.py"))
scan = importlib.util.module_from_spec(_scan)
_scan.loader.exec_module(scan)

ABILITY = re.compile(r"\baddTrigger\(|new TriggeredAbility\(")
TRIGGER = re.compile(r"^Triggers\.(\w+)\((.*)\)((?:\.\w+\((?:[^()]|\([^()]*\))*\))*)$", re.S)
# Factories with a (filter, relation) form, and the 1-argument forms that gain one.
ONE_ARGUMENT = {"dies": "dies", "sacrificed": "sacrificed", "leavesBattlefield": "leavesBattlefield",
                "creatureDies": "creatureDies", "nontokenCreatureDies": "nontokenCreatureDies"}

SKIP = {
    # "if it wasn't sacrificed": no trigger option excludes a cause yet.
    "UrzasMiter.java",
    # "The first time one or more land cards are put into your graveyard each turn": the collector counts the
    # events before this entered, which no limit says.
    "CrawlingSensation.java",
    # "If it's a creature" about the permanent turned face up, read from the stack entry's target as it resolves.
    "TrailOfMystery.java",
    # "Put into your graveyard from anywhere other than the battlefield this turn": a card predicate about the
    # card's history, with no permanent form.
    "DisaTheRestless.java",
    # Its transforms half shares "do this only once each turn" with an enters half, which run 3b converts.
    "CorruptionOfTowashi.java",
    # "The first time one or more land cards leave your graveyard each turn" shares its once-each-turn key with the
    # card's own enters abilities (run 4).
    "OasisOfRenewal.java",
}

MANUAL = {}
MANUAL_IMPORTS = {}

IMPORTS = dict(do.IMPORTS)
IMPORTS.update({name: do.FILTER + name for name in [
    "PermanentIsLandPredicate", "PermanentIsEnchantmentPredicate", "PermanentIsHistoricPredicate",
    "PermanentIsPlaneswalkerPredicate", "PermanentHasAnySubtypePredicate", "PermanentIsLegendaryPredicate",
    "PermanentIsPermanentCardPredicate", "PermanentIsNonlandPredicate", "PermanentManaValueAtMostPredicate"]})
IMPORTS["ObjectRelation"] = "com.github.laxika.magicalvibes.model.trigger.ObjectRelation"
IMPORTS["Triggers"] = "com.github.laxika.magicalvibes.model.trigger.Triggers"

DEFAULT_ZONE = re.compile(r"AbilityZones\.(\w+)")


def family_trigger(trigger):
    """(factory, leading args, filter, relation, suffix) of a 3a trigger expression, or None for another trigger."""
    m = TRIGGER.match(trigger.strip())
    if not m:
        return None
    name, args, suffix = m.group(1), po.split_args(m.group(2)), m.group(3)
    if not scan.in_family(name, args) or name in ("enters", "entersFrom", "tokensCreated"):
        return None
    if name in ("transformsToBackFace", "transformsToFrontFace"):
        return name, [], None, args[-1], suffix
    if len(args) == 1:
        return name, [], None, args[0], suffix
    return name, args[:-2], args[-2], args[-1], suffix


PERMANENT_CHANGE = {"phasesOut", "phasesIn", "transforms", "transformsToBackFace", "transformsToFrontFace",
                    "turnedFaceUp"}


def build_trigger(parts, filters, your_turn, relation=None):
    name, leading, flt, rel, suffix = parts
    rel = relation or rel
    if filters:
        extra = filters[0] if len(filters) == 1 else "new PermanentAllOfPredicate(List.of(%s))" % ", ".join(filters)
        base = None if flt in (None, "null") else flt
        if name == "creatureDies":
            base = base or "Triggers.CREATURE"
            name = "dies"
        elif name == "nontokenCreatureDies":
            base = base or "Triggers.NONTOKEN_CREATURE"
            name = "dies"
        elif name in ("transformsToBackFace", "transformsToFrontFace"):
            raise ValueError("no filter form of " + name)
        flt = extra if base is None else "new PermanentAllOfPredicate(List.of(%s, %s))" % (base, extra)
    if your_turn and ".onlyDuringYourTurn()" not in suffix:
        if name in PERMANENT_CHANGE:
            raise ValueError("no onlyDuringYourTurn() on " + name)
        suffix += ".onlyDuringYourTurn()"
    if flt is None and name in ONE_ARGUMENT:
        return "Triggers.%s(%s)%s" % (name, rel, suffix)
    if flt is None and name in ("transformsToBackFace", "transformsToFrontFace"):
        return "Triggers.%s(%s)%s" % (name, rel, suffix)
    return "Triggers.%s(%s)%s" % (name, ", ".join(leading + [flt or "null", rel]), suffix)


def unwrap(effect):
    """(filters, relation, limit, condition, during your turn, effect, effect list) with the outermost trigger-only
    wrappers taken off. The effect list is set when a wrapper held several effects."""
    filters, relation, limit, condition, your_turn, effects = [], None, None, None, False, None

    def set_limit(value):
        nonlocal limit
        if limit is not None:
            raise ValueError("two limits: " + effect)
        limit = value

    def set_condition(value):
        nonlocal condition, your_turn
        if do.strip_fq(value) == "new ControllerTurn()":
            your_turn = True
        elif condition is None:
            condition = value
        else:
            raise ValueError("two intervening ifs: " + effect)

    def set_relation(value):
        nonlocal relation
        if relation is not None:
            raise ValueError("two relations: " + effect)
        relation = value

    while True:
        args = po.call(effect, "new TriggeringPermanentConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] in ("true", "false"))):
            if args[0] != "null":
                filters.append(args[0])
            effect = args[1]
            continue
        args = po.call(effect, "new TriggeringCardConditionalEffect")
        if args is not None and len(args) == 2:
            filters.append(do.card_to_permanent(args[0]))
            effect = args[1]
            continue
        args = po.call(effect, "new EnchantedPermanentLeavesConditionalEffect")
        if args is not None and len(args) == 2:
            if args[0] != "null":
                filters.append(do.card_to_permanent(args[0]))
            listed = po.call(args[1], "List.of")
            if listed is None:
                raise ValueError("an effect list that isn't List.of: " + effect)
            effects = listed
            effect = None
            break
        args = po.call(effect, "new TriggeringPermanentControllerConditionalEffect")
        if args is not None and len(args) in (1, 2):
            if len(args) == 2 and args[1] != "true":
                set_relation("ANOTHER_YOU_CONTROL")
            else:
                set_relation("YOU_CONTROL")
            effect = args[0]
            continue
        args = po.call(effect, "TriggeringPermanentControllerConditionalEffect.another")
        if args is not None and len(args) == 1:
            set_relation("ANOTHER_YOU_CONTROL")
            effect = args[0]
            continue
        args = po.call(effect, "new TriggeringArtifactControllerConditionalEffect")
        if args is not None and 1 <= len(args) <= 4:
            flags = args[1:] + ["false"] * (4 - len(args))
            if flags[0] != "false":
                raise ValueError("'if it wasn't sacrificed': " + effect)
            if flags[1] == "true":
                filters.append("Triggers.NONTOKEN")
            set_relation("ANOTHER_YOU_CONTROL" if flags[2] == "true" else "YOU_CONTROL")
            effect = args[0]
            continue
        args = po.call(effect, "TriggeringArtifactControllerConditionalEffect.another")
        if args is not None and len(args) == 1:
            set_relation("ANOTHER_YOU_CONTROL")
            effect = args[0]
            continue
        args = po.call(effect, "new ConditionalTriggeringPermanentEffect")
        if args is not None and len(args) == 3:
            set_condition(args[0])
            filters.append(args[1])
            effect = args[2]
            continue
        once = po.call(effect, "new OncePerTurnTriggerEffect")
        keyed = po.call(effect, "OncePerTurnTriggerEffect.keyed")
        accept = po.call(effect, "OncePerTurnTriggerEffect.markOnAcceptance")
        only = po.call(effect, "new OnceOnlyTriggerEffect")
        if once is not None and len(once) == 1:
            set_limit("ONCE_EACH_TURN")
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
        args = po.call(effect, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
            if do.strip_fq(args[0]).startswith("new SourceCardInGraveyard("):
                break
            set_condition(args[0])
            effect = args[1]
            continue
        if re.match(r"^(new )?(OncePerTurnTriggerEffect|OnceOnlyTriggerEffect|TriggeringPermanentControllerConditional|"
                    r"TriggeringArtifactControllerConditional|ConditionalTriggeringPermanentEffect|"
                    r"EnchantedPermanentLeavesConditional|TriggeringPermanentConditional|TriggeringCardConditional)",
                    do.strip_fq(effect)) or re.match(r"^ConditionalEffect\.atTriggerTime", do.strip_fq(effect)):
            raise ValueError("an unknown wrapper: " + effect)
        break
    return filters, relation, limit, condition, your_turn, effect, effects


def relation_of(parts, narrowed):
    if narrowed is None:
        return None
    current = parts[3].replace("ObjectRelation.", "")
    if current != "ANY":
        raise ValueError("a controller wrapper on relation " + current)
    return "ObjectRelation." + narrowed


def convert(src, name):
    """Converts every 3a ability of the card; returns the new source and the names it needs."""
    needs = set()
    edits = []
    var_edits = {}
    spans = []
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
        filters, narrowed, limit, condition, your_turn, inner, inner_list = unwrap(effect)
        if your_turn and parts[0] in PERMANENT_CHANGE:
            # "If it's your turn" (CR 603.4): a permanent change has no onlyDuringYourTurn().
            if condition is not None:
                raise ValueError("two intervening ifs")
            condition, your_turn = "new ControllerTurn()", False
        if not filters and not narrowed and not limit and not condition and not your_turn and inner_list is None:
            continue
        if m.group(0).startswith("new") and (limit or condition):
            raise ValueError("options on a granted ability: " + src[m.start():end])
        if inner_list is not None and variable:
            raise ValueError("an effect list behind a variable")
        new_trigger = build_trigger(parts, filters, your_turn, relation_of(parts, narrowed))
        if existing and not (limit or condition):
            new_options = existing
        elif existing or limit or condition:
            new_options = do.options_expr(existing, limit, condition)
        else:
            new_options = None
        if limit:
            needs.add("TriggerLimit")
        if new_options and "TriggerOptions" in new_options:
            needs.add("TriggerOptions")
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
        if condition_form is None and new_options and not existing:
            # addTrigger(Trigger, TriggerOptions, CardEffect...) exists for the plain form.
            pass
        line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(line) - len(line.lstrip()) + 8)
        tail = inner_list if inner_list is not None else [inner]
        body = do.layout([first] + ([new_options] if new_options else []) + tail, indent,
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
        elif imp in ("ObjectRelation", "Triggers"):
            if re.search(r"\b%s\." % imp, src):
                needs.add(imp)
        elif re.search(r"\bnew %s\(" % imp, src):
            needs.add(imp)
    return src, needs


WRAPPER_TYPES = do.WRAPPER_TYPES | {"TriggeringPermanentControllerConditionalEffect",
                                    "TriggeringArtifactControllerConditionalEffect", "OnceOnlyTriggerEffect",
                                    "ConditionalTriggeringPermanentEffect", "EnchantedPermanentLeavesConditionalEffect"}


def _pretty_trigger(flat, inner):
    trigger = TRIGGER.match(flat)
    if not trigger:
        return None
    parts = po.split_args(trigger.group(2))
    return "Triggers.%s(\n%s%s)%s" % (trigger.group(1), inner, (",\n" + inner).join(
        do.pretty(part, inner, len(inner)) for part in parts), trigger.group(3))


do.DIES = TRIGGER

_card_to_permanent = do.card_to_permanent
CARD_TYPES = {"CardType.ARTIFACT": "new PermanentIsArtifactPredicate()",
              "CardType.CREATURE": "new PermanentIsCreaturePredicate()",
              "CardType.LAND": "new PermanentIsLandPredicate()",
              "CardType.ENCHANTMENT": "new PermanentIsEnchantmentPredicate()",
              "CardType.PLANESWALKER": "new PermanentIsPlaneswalkerPredicate()"}


def card_to_permanent(expr):
    """The run 2 conversion, with the card types and predicates this family's cards use."""
    flat = do.strip_fq(expr)
    args = po.call(flat, "new CardTypePredicate")
    if args is not None and args[0] in CARD_TYPES:
        return CARD_TYPES[args[0]]
    for card, perm in (("new CardIsHistoricPredicate", "new PermanentIsHistoricPredicate()"),
                       ("new CardIsPermanentPredicate", "new PermanentIsPermanentCardPredicate()")):
        if po.call(flat, card) is not None:
            return perm
    for name in ("new CardNotPredicate", "new CardAllOfPredicate", "new CardAnyOfPredicate"):
        args = po.call(flat, name)
        if args is not None:
            if name == "new CardNotPredicate":
                return "new PermanentNotPredicate(%s)" % card_to_permanent(args[0])
            parts = po.call(args[0], "List.of")
            if parts is None:
                raise ValueError("unknown card predicate list: " + expr)
            perm = "PermanentAllOfPredicate" if "AllOf" in name else "PermanentAnyOfPredicate"
            return "new %s(List.of(%s))" % (perm, ", ".join(card_to_permanent(p) for p in parts))
    return _card_to_permanent(expr)


do.card_to_permanent = card_to_permanent


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
                    if new and new in src:
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
                src = po.add_imports(src, [IMPORTS[n] for n in sorted(needs)])
                src = po.drop_unused_imports(original, src)
                src = re.sub(r"(^import com\.[^\n]+;\n)(import java\.)", r"\1\n\2", src, flags=re.M)
                if dry:
                    print("=== " + path)
                    import difflib
                    sys.stdout.writelines(difflib.unified_diff(original.splitlines(True), src.splitlines(True), n=0))
                else:
                    open(path, "w").write(src)
                    print(path)




# Exact replacements applied after the conversion, each skipped once its new text is in the card: the "this or
# another" cards whose two halves become one ability of two trigger conditions (each still collected where it was:
# the source's own departure by the pilot path, another permanent's by its watch), and the cards the conversion
# can't express. A pair is (old, new).
TWO_CONDITIONS = ["TriggeredAbility", "TriggerCondition", "EffectRegistration", "List"]
AFTER = {
    "ZoneOfFlame.java": [(
        """        addTrigger(Triggers.leavesBattlefield(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        ObjectRelation.ANOTHER),
                trigger);
        addTrigger(Triggers.leavesBattlefield(Triggers.NONTOKEN, ObjectRelation.THIS), trigger);""",
        """        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.NONTOKEN, ObjectRelation.ANOTHER)),
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.NONTOKEN, ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(trigger))));""")],
    "ResourcefulDefense.java": [(
        """        targetPermanentYouControl.addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.ANY)),
                transferCounters);
        targetPermanentYouControl.addTrigger(Triggers.leavesBattlefield(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                transferCounters);""",
        """        // "A permanent you control ... if it had counters on it", this one included: one ability with two trigger
        // conditions and the same filter, judged on the permanent as it last existed (CR 603.10a).
        var hadCounters = new PermanentHasCountersPredicate(CounterType.ANY);
        targetPermanentYouControl.addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(hadCounters, ObjectRelation.THIS)),
                new TriggerCondition(Triggers.leavesBattlefield(hadCounters, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(transferCounters))));""")],
    "NadiersNightblade.java": [(
        """        addTrigger(Triggers.leavesBattlefield(new PermanentIsTokenPredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                drainOpponentAndGainLife);
        // "a token you control leaves the battlefield" includes this token itself: a leaves-the-battlefield
        // ability looks back in time (CR 603.10a).
        addTrigger(Triggers.leavesBattlefield(Triggers.TOKEN, ObjectRelation.THIS), drainOpponentAndGainLife);""",
        """        // "A token you control leaves the battlefield" includes a token copy of this creature: a
        // leaves-the-battlefield ability looks back in time (CR 603.10a).
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.TOKEN, ObjectRelation.ANOTHER_YOU_CONTROL)),
                new TriggerCondition(Triggers.leavesBattlefield(Triggers.TOKEN, ObjectRelation.THIS))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(drainOpponentAndGainLife))));""")],
    "CostumeCloset.java": [(
        """        addTrigger(Triggers.leavesBattlefield(
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentIsModifiedPredicate())),
                        ObjectRelation.THIS),
                new PutCountersOnSourceEffect(1, 1, 1));

        addTrigger(Triggers.leavesBattlefield(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsModifiedPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));""",
        """        // "A modified creature you control", this artifact included once it is one.
        var modifiedCreature = new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                new PermanentIsModifiedPredicate()));
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(modifiedCreature, ObjectRelation.THIS)),
                new TriggerCondition(Triggers.leavesBattlefield(modifiedCreature, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(new PutCountersOnSourceEffect(1, 1, 1)))));""")],
    # "Another creature or artifact you control": the controller wrapper's body left the source out.
    "ExuberantFuseling.java": [(
        """                                new PermanentIsArtifactPredicate())),
                        ObjectRelation.YOU_CONTROL),""",
        """                                new PermanentIsArtifactPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),""")],
    "GrahaTia.java": [(
        """                                new PermanentIsArtifactPredicate())),
                        ObjectRelation.YOU_CONTROL),""",
        """                                new PermanentIsArtifactPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),""")],
    # Lines the conversion left past 120 columns.
    "DefiledCryptCadaverLab.java": [(
        """                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN).withInterveningIf(new SourceRoomDoorUnlocked(0)),""",
        """                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN)
                        .withInterveningIf(new SourceRoomDoorUnlocked(0)),""")],
    "Moonshadow.java": [(
        """                TriggerOptions.ONE_OR_MORE.withInterveningIf(new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),""",
        """                TriggerOptions.ONE_OR_MORE
                        .withInterveningIf(new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),""")],
    "KithkinMourncaller.java": [(
        """                                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.KITHKIN, CardSubtype.ELF)))))),""",
        """                                        new PermanentHasAnySubtypePredicate(
                                                Set.of(CardSubtype.KITHKIN, CardSubtype.ELF)))))),""")],
    "ThendarTheOverminer.java": [(
        """        var nonbasicLand = new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(), new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));""",
        """        var nonbasicLand = new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));""")],
}
AFTER_IMPORTS = {
    "ZoneOfFlame.java": TWO_CONDITIONS,
    "ResourcefulDefense.java": TWO_CONDITIONS,
    "NadiersNightblade.java": TWO_CONDITIONS + ["TriggerOptions"],
    "CostumeCloset.java": TWO_CONDITIONS + ["TriggerOptions"],
}
# Long Feng, Grand Secretariat and Projektor Inspector were converted by hand: a comment between the arguments of the
# first, a helper method shared with an enters ability (run 3b) in the second (effect-slot-refactor.md, step 14 run 3a).


def apply_after(path):
    name = os.path.basename(path)
    if name not in AFTER:
        return
    src = open(path).read()
    original = src
    for old, new in AFTER[name]:
        if new in src:
            continue
        if old not in src:
            raise SystemExit("AFTER replacement not found in " + path)
        src = src.replace(old, new)
    if src == original:
        return
    src = po.add_imports(src, [IMPORTS[n] for n in AFTER_IMPORTS.get(name, [])])
    src = po.drop_unused_imports(original, src)
    src = re.sub(r"(^import com\.[^\n]+;\n)(import java\.)", r"\1\n\2", src, flags=re.M)
    open(path, "w").write(src)
    print(path)


def after_main():
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if name in AFTER:
                    apply_after(os.path.join(root, name))




# "This [permanent] or another [restricted] one": the card's own half and the other half become one ability of two
# trigger conditions, each kept as it was (the own half still collected by the pilot path, the other by its watch).
# Psychomancer stays split: its halves are a target group's chain, with a third, handler-level exile half.
MERGE = {"VelaTheNightClad.java", "SplinterTheMentor.java", "SlayersBounty.java", "ScrapTrawler.java",
         "SlagstoneRefinery.java", "RatKingPalePiper.java", "EsotericDuplicator.java"}


def condition_line(trigger, indent):
    """{@code new TriggerCondition(trigger)} under {@code indent}, its trigger's arguments wrapped past 120 columns."""
    flat = "new TriggerCondition(%s)" % do.collapse(trigger)
    if len(indent) + len(flat) + 2 <= 120:
        return indent + flat
    m = TRIGGER.match(do.collapse(trigger))
    parts = po.split_args(m.group(2))
    inner = indent + " " * 16
    head = "%snew TriggerCondition(Triggers.%s(%s," % (indent, m.group(1), ", ".join(parts[:-1]))
    if len(head) <= 120:
        return "%s\n%s%s)%s)" % (head, inner, parts[-1], m.group(3))
    return "%snew TriggerCondition(Triggers.%s(\n%s%s)%s)" % (indent, m.group(1), inner, (",\n" + inner).join(
        do.pretty(part, inner, len(inner)) for part in parts), m.group(3))


def merge_halves(path):
    src = open(path).read()
    statements = []
    for m in re.finditer(r"(?m)^([ \t]*)addTrigger\(", src):
        end = po.close_paren(src, m.end())
        if src[end] != ";":
            continue
        statements.append((m.start(), end + 1, m.group(1), po.split_args(src[m.end():end - 1])))
    own = [s for s in statements if len(s[3]) == 2 and re.search(r"ObjectRelation\.THIS\)$", s[3][0])
           and not s[3][0].startswith("Triggers.enters")]
    other = [s for s in statements if len(s[3]) == 2 and family_trigger(s[3][0]) and "ANOTHER" in s[3][0]]
    pairs = [(a, b) for a in own for b in other if re.sub(r"\s", "", a[3][1]) == re.sub(r"\s", "", b[3][1])]
    if not pairs:
        return
    if len(pairs) != 1:
        raise SystemExit("%s: %d own/other pairs" % (path, len(pairs)))
    own_half, other_half = pairs[0]
    indent = own_half[2]
    inner = indent + " " * 8
    merged = "%saddTriggeredAbility(new TriggeredAbility(List.of(\n%s,\n%s),\n%sTriggerOptions.DEFAULT, " \
             "List.of(new EffectRegistration(%s))));" % (
                 indent, condition_line(other_half[3][0], inner), condition_line(own_half[3][0], inner), inner,
                 do.collapse(own_half[3][1]))
    first, second = sorted([own_half, other_half])
    # The merged ability takes the first half's place; the second half's line goes.
    second_end = second[1] + (1 if src[second[1]:second[1] + 1] == "\n" else 0)
    src = src[:first[0]] + merged + src[first[1]:second[0]] + src[second_end:]
    src = po.add_imports(src, [IMPORTS[n] for n in TWO_CONDITIONS + ["TriggerOptions"]])
    src = re.sub(r"(^import com\.[^\n]+;\n)(import java\.)", r"\1\n\2", src, flags=re.M)
    open(path, "w").write(src)
    print(path)


def merge_main():
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if name in MERGE:
                    merge_halves(os.path.join(root, name))


if __name__ == "__main__":
    main()
    if "--dry-run" not in sys.argv:
        after_main()
        merge_main()
