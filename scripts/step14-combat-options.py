#!/usr/bin/env python3
"""Step 14, run 7 of effect-slot-refactor.md: the trigger-only wrappers of the combat abilities (Triggers.attacks,
creatureYouControlAttacks, youAttack, blocks, becomesBlocked, attacksUnblocked, dealsCombatDamageToPlayer,
creatureYouControlDealsCombatDamageToPlayer, …, read by the CombatWatch collectors) become the trigger's filter and
the ability's TriggerOptions. The collectors read the abilities through TriggeredAbility.legacyEffects(), which gives
them the same wrappers and trigger modes again, so nothing they see changes.

The wrappers, outermost first, as the cards nest them and the legacy view rebuilds them:

  OncePerTurnTriggerEffect (new …, keyed, markOnAcceptance)  →  .withLimit(ONCE_EACH_TURN / DO_ONLY_ONCE_EACH_TURN)
  TriggeringPermanentConditionalEffect(p, x)                 →  trigger.withFilter(p)
  TriggeringCardConditionalEffect(c, x)                       →  trigger.withFilter(<c as a permanent predicate>), on
                                                                 creatureYouControlAttacks only (a rules fix: the
                                                                 attacking creature's characteristics, not its card's)
  AllyCombatDamageTriggerEffect(p, x, bind, once, opponents)  →  trigger.withFilter(p), ONE_OR_MORE for `once`; `bind`
                                                                 and `opponents` stay as boundToDealer(x) /
                                                                 opponentsDealtDamage(x), the event bindings step 15
                                                                 replaces
  BatchedCombatDamageToYouTriggerEffect(x)                    →  TriggerOptions.ONE_OR_MORE
  ConditionalEffect(c, x) / (c, x, true)                      →  .withInterveningIf(c), when it is an intervening "if"
  OnceOnlyTriggerEffect(x)                                    →  .withLimit(ONCE)

and the registration's TriggerMode.ONCE_PER_BLOCK / PER_BLOCKER become TriggerOptions.ONE_OR_MORE / EACH_BLOCKER
(CR 509.3a, 509.3d). INDEPENDENT stays (run 16).

Every effect of an ability must carry the same wrappers; an effect passed as a local variable is followed to its one
definition. An ability with a wrapper the legacy view can't rebuild in place (TriggeringPermanentControllerConditional-
Effect, a flagged TriggeringPermanentConditionalEffect, a limit inside the intervening "if", a card filter on another
trigger) is KEPT and reported.

Which ConditionalEffects are intervening "if"s is decided by the Oracle text: the condition is part of the trigger
event (QUALIFIERS: "attacks alone", "with three or more creatures", "attacks a player", a Class level, a chosen mode,
saddled, …), or the card's text has a "When/Whenever … [attack/block/combat damage] …, if" sentence for each wrapped
ability, or the card is in INTERVENING_CARDS. NOT_INTERVENING lists the effect-text "if"s, which stay effects. Any other
wrapped ability stops the script (UNDECIDED), so no card is decided silently. The legacy view rebuilds an intervening
"if" exactly as the ConditionalEffect was, so the decision changes no behaviour.

A second pass merges "whenever this creature attacks or blocks" (an attacks and a blocks ability with the same
effects, MERGE-able when the Oracle text says "attacks or blocks") into one ability with both trigger conditions.

--dry-run prints the diffs, --report the decision for each ability.
Usage: python3 scripts/step14-combat-options.py [--dry-run] [--report] <module dir>...
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


do = _load("death_options", "step14-death-options.py")
po = do.po
pilot = _load("pilot_scan", "step14-pilot-scan.py")
scan = _load("combat_scan", "step14-combat-scan.py")

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(")
FAMILY = scan.FAMILY
ALLY_DAMAGE = {"creatureYouControlDealsCombatDamageToPlayer", "creatureYouControlDealsCombatDamageToPlayerOrBattle",
               "creatureYouControlDealsCombatDamageToPlayerOrPlaneswalker"}
BLOCK_ONE_OR_MORE = {"blocks", "creatureBlocks", "becomesBlocked", "creatureYouControlBecomesBlocked",
                     "creatureBecomesBlocked"}
BECOMES_BLOCKED = {"becomesBlocked", "creatureYouControlBecomesBlocked", "creatureBecomesBlocked"}

# Conditions that are part of the trigger event, or that say the ability exists at all (a Class level, a chosen
# mode, an unlocked door, saddled, solved): intervening "if"s whatever the Oracle words, as runs 1 to 6 made them.
QUALIFIERS = {
    "AttacksAlone", "AttacksPlayerAlone", "MinimumAttackers", "ExactlyAttackers", "MinimumMatchingAttackers",
    "MinimumAttackingCreaturesOfSubtype", "AttackingCreaturesTotalPowerAtLeast", "HasAttacker",
    "AttacksEnchantedPlayer", "OpponentAttacksWithAtLeastCreatures", "OpponentsAttackWithAtLeastCreatures",
    "AttackedTargetIsOpponent", "AttackedTargetMatches", "AttackedTargetIsMonarch", "PlayerAttacksOneOfYourOpponents",
    "PlayerAttacksOneOrMoreOfYourOpponents", "PlayerAttacksNotController", "OpponentAttacksAnotherOpponent",
    "OpponentAttacksPlaneswalker", "ControllerAndEnchantedPlayerAttackEachOther", "SourceAttackingPlayer",
    "AllMatchingCreaturesAttack", "AttackedOpponentHasMoreLifeThanAnotherOpponent",
    "PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber", "BlockedByMinCreatures",
    "SourceBlocksWithAtLeastAndOnlyMatchingBlockers", "AttackingCreaturesGreaterThanSourceCounters",
    "SourceIsSaddled", "SourceClassLevelAtLeast", "SourceHasChosenMode", "SourceRoomDoorUnlocked", "SourceIsSolved",
    "AttackedTargetIsMonarch",
}

# Cards whose wrapped combat abilities are all intervening "if"s though the Oracle text doesn't say "Whenever …, if"
# once for each of them.
INTERVENING_CARDS = {
    # One Oracle ability split over two abilities (two watches, or two effects), each with the same intervening "if".
    "ArchivistOfGondor.java", "RaphLeoSiblingRivals.java", "TurfWar.java", "VengefulPharaoh.java",
    "WildwoodTracker.java",
    # A Class level or a station threshold: the ability exists only from it on (CR 716.2a, 721.2a).
    "CandelaAegisOfAdagia.java", "FighterClass.java", "PaladinClass.java", "RangerClass.java",
    # Part of the trigger event: "attack with this creature and/or your commander", "attacks alone", "attacks while
    # you don't control a legendary Monkey", "a player who is afraid of you", "during your turn".
    "AinokStrikeLeader.java", "IronsoulEnforcer.java", "KariZevCrewOfTwo.java", "CreepyCrawler.java",
    "QuilledGreatwurm.java", "TheAetherspark.java", "FiremaneCommando.java",
    # Double team: "When this creature attacks, if it's not a token, conjure a duplicate of it into your hand."
    "EnduringFriendship.java",
    # A back face without Oracle text of its own: "Whenever Starscream deals combat damage to a player, if there is
    # no monarch, that player becomes the monarch."
    "StarscreamSeekerLeader.java",
}

# Wrappers that are the effect text's own "if", not an intervening "if": they stay effects. Card → the conditions
# kept (substrings of the condition expression; empty: every wrapped combat ability of the card).
NOT_INTERVENING = {
    # "you may pay {3} and sacrifice it. If you do, …"
    "CanoptekWraith.java": [],
    # "remove a shield counter from it. If you do, draw a card."
    "FamilysFavor.java": [],
    # "If her sneak cost was paid this turn, instead return that card to the battlefield."
    "KaraiFutureOfTheFoot.java": [],
    # "you win the game if there are twenty or more counters on it or you have twenty or more cards in hand"
    "TwentyToedToad.java": [],
    # "If Mishra, Claimed by Gix and a creature named Phyrexian Dragon Engine are attacking, …, exile them, then meld"
    "MishraClaimedByGix.java": [],
}

# Abilities left as they are, with the reason (card → substrings of the ability; empty: every one of the card).
KEEP = {}

IMPORTS = {
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "TriggerLimit": "com.github.laxika.magicalvibes.model.trigger.TriggerLimit",
    "CardEffect": "com.github.laxika.magicalvibes.model.effect.CardEffect",
    "AllyCombatDamageTriggerEffect": "com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect",
    "List": "java.util.List",
    "Set": "java.util.Set",
    "PermanentPredicate": "com.github.laxika.magicalvibes.model.filter.PermanentPredicate",
}
for _name in ["PermanentHasSubtypePredicate", "PermanentIsTokenPredicate", "PermanentNotPredicate",
              "PermanentColorInPredicate", "PermanentHasSupertypePredicate", "PermanentIsMulticoloredPredicate",
              "PermanentHasKeywordPredicate", "PermanentMinManaValuePredicate",
              "PermanentHasSourceChosenSubtypePredicate", "PermanentIsArtifactPredicate",
              "PermanentIsCreaturePredicate", "PermanentAllOfPredicate", "PermanentAnyOfPredicate"]:
    IMPORTS[_name] = "com.github.laxika.magicalvibes.model.filter." + _name


class Keep(Exception):
    """An ability the script leaves as it is, with the reason."""


def norm(text):
    return re.sub(r"\s+", "", text)


def bare(expr):
    return do.strip_fq(expr.strip())


def peel(effect):
    """The wrappers of one effect expression, outermost first, as (kind, data) pairs, and the effect inside them."""
    chain = []
    while True:
        e = effect.strip()
        args = po.call(e, "new OncePerTurnTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("L", "ONCE_EACH_TURN")); effect = args[0]; continue
        args = po.call(e, "OncePerTurnTriggerEffect.keyed")
        if args is not None and len(args) == 2:
            chain.append(("L", "ONCE_EACH_TURN")); effect = args[0]; continue
        args = po.call(e, "OncePerTurnTriggerEffect.markOnAcceptance")
        if args is not None and len(args) == 1:
            chain.append(("L", "DO_ONLY_ONCE_EACH_TURN")); effect = args[0]; continue
        if re.match(r"(new )?OncePerTurnTriggerEffect\b", bare(e)):
            raise Keep("a once-each-turn wrapper the limit can't say: " + e[:60])
        args = po.call(e, "new OnceOnlyTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("O", None)); effect = args[0]; continue
        args = po.call(e, "new TriggeringPermanentConditionalEffect")
        if args is not None:
            if len(args) != 2:
                raise Keep("a flagged TriggeringPermanentConditionalEffect")
            chain.append(("P", args[0])); effect = args[1]; continue
        if bare(e).startswith("TriggeringPermanentConditionalEffect."):
            raise Keep("a TriggeringPermanentConditionalEffect factory")
        args = po.call(e, "new TriggeringCardConditionalEffect")
        if args is not None:
            if len(args) != 2:
                raise Keep("a flagged TriggeringCardConditionalEffect")
            chain.append(("C", args[0])); effect = args[1]; continue
        if "TriggeringPermanentControllerConditionalEffect" in bare(e).split("(")[0]:
            raise Keep("TriggeringPermanentControllerConditionalEffect: \"a creature you control\" on a watch of any "
                       "creature's")
        args = po.call(e, "new AllyCombatDamageTriggerEffect")
        if args is not None:
            flags = [a == "true" for a in args[2:]] + [False] * (5 - len(args))
            if any(a not in ("true", "false") for a in args[2:]):
                raise Keep("an AllyCombatDamageTriggerEffect flag that isn't a literal")
            chain.append(("A", (args[0], flags[0], flags[1], flags[2]))); effect = args[1]; continue
        args = po.call(e, "new BatchedCombatDamageToYouTriggerEffect")
        if args is not None and len(args) == 1:
            chain.append(("B", None)); effect = args[0]; continue
        args = po.call(e, "new ConditionalEffect")
        if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
            chain.append(("I", args[0])); effect = args[1]; continue
        return chain, effect


ORDER = {"L": 0, "P": 1, "C": 1, "A": 2, "B": 2, "I": 3, "O": 4}


def condition_class(condition):
    m = re.match(r"new ([\w.]+)\(", bare(condition))
    return m.group(1).rsplit(".", 1)[-1] if m else None


def abilities(src):
    """(match, end, args, mode, options, effects) of each combat ability statement of the source."""
    for m in ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        if not args or not FAMILY.search(args[0]):
            continue
        rest = args[1:]
        mode = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]) else None
        options = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerOptions\b", rest[0]) else None
        yield m, end, args, mode, options, rest


def factory(trigger_arg):
    return FAMILY.search(trigger_arg).group(1)


def kept(table, name, text):
    if name not in table:
        return False
    parts = table[name]
    return not parts or any(part in text for part in parts)


def intervening_sentences(oracle):
    return re.findall(r"\bWhen(?:ever)?\b[^.\n|]*?\b(?:attack|block|combat damage|dealt)\w*\b[^.\n|]*?,\s*if\b",
                      oracle, re.I)


def options_expr(base, condition, limit, indent):
    tail = ".withLimit(TriggerLimit.%s)" % limit if limit else ""
    if condition is None:
        return base + tail
    flat = do.collapse(condition)
    head = base + ".withInterveningIf("
    if len(indent) + len(head) + len(flat) + len(tail) + 2 <= 120:
        return head + flat + ")" + tail
    inner = indent + " " * 8
    if len(inner) + len(flat) + len(tail) + 2 <= 120:
        return head + "\n" + inner + flat + ")" + tail
    return head + "\n" + inner + do.reindent(condition, inner) + ")" + tail


def with_filter(trigger, predicate, needs):
    """The trigger expression narrowed by `predicate` (inside a `new TriggerCondition(…)` when it is one)."""
    flat = do.collapse(predicate)
    condition = po.call(bare(trigger), "new TriggerCondition")
    if condition is not None:
        inner = condition[0]
        return "new TriggerCondition(%s, %s)" % (with_filter(inner, predicate, needs), ", ".join(condition[1:]))
    if not re.fullmatch(r"(?:[\w.]+\.)?Triggers\.\w+\(\)", bare(trigger)):
        raise Keep("a combat trigger that isn't a plain factory call: " + trigger)
    return bare(trigger) + ".withFilter(" + predicate.strip() + ")"


def break_filter(trigger, column, indent):
    """A filtered plain trigger that doesn't fit before column 118, with `.withFilter(…)` on the next line."""
    if not trigger.startswith("Triggers.") or ".withFilter(" not in trigger:
        return trigger
    head, tail = trigger.split(".withFilter(", 1)
    predicate = tail[:-1]
    flat = do.collapse(predicate)
    if column + len(head) + len(".withFilter(") + len(flat) + 3 <= 118:
        return head + ".withFilter(" + flat + ")"
    inner = indent + " " * 8
    start = len(inner) + len(".withFilter(")
    return head + "\n" + inner + ".withFilter(" + do.pretty(predicate, inner, start) + ")"


def add_imports(src, names):
    """Adds the imports, a java.* one in its own block after the others when the card has none yet."""
    plain = [n for n in names if not n.startswith("java.")]
    src = po.add_imports(src, plain)
    for fq in [n for n in names if n.startswith("java.")]:
        simple = fq.rsplit(".", 1)[1]
        if re.search(r"^import [\w.]+\." + simple + ";", src, re.M):
            continue
        if re.search(r"^import java\.", src, re.M):
            src = po.add_imports(src, [fq])
            continue
        imports = list(re.finditer(r"^import (?!static)[\w.]+;\n", src, re.M))
        src = src[:imports[-1].end()] + "\nimport " + fq + ";\n" + src[imports[-1].end():]
    return src


def resolve(effects, src):
    """Per effect: (variable, chain, inner)."""
    out = []
    for effect in effects:
        variable = None
        text = effect
        if re.fullmatch(r"[a-z]\w*", effect.strip()):
            found = do.definition(src, effect.strip())
            if found is None:
                raise Keep("an effect variable without one definition: " + effect.strip())
            variable = (effect.strip(), found)
            text = found[2]
        chain, inner = peel(text)
        out.append((variable, chain, inner))
    return out


def decide(name, src, oracle, report, counts):
    """The edits of the card's combat abilities: (start, end, text) and the variable edits."""
    edits, var_edits, spans, needs = [], {}, [], set()
    for m, end, args, mode, options, effects in abilities(src):
        text = re.sub(r"\s+", " ", src[m.start():end])
        trig = factory(args[0])
        try:
            if kept(KEEP, name, text):
                raise Keep("listed")
            if not effects:
                continue
            if options is not None and norm(bare(options)) != "TriggerOptions.DEFAULT":
                raise Keep("already has options")
            resolved = resolve(effects, src)
            combat_spans = [(a.start(), e) for a, e, *_ in abilities(src)]
            for variable, chain_, _ in resolved:
                if variable is None or not chain_:
                    continue
                vname, (vstart, vend, _) = variable
                for use in re.finditer(r"\b%s\b" % re.escape(vname), src):
                    if vstart <= use.start() < vend or re.match(r"\s*=", src[use.end():]):
                        continue
                    if not any(a <= use.start() < b for a, b in combat_spans):
                        raise Keep("its effect variable %s is shared with another family's ability" % vname)
            chains = {tuple((k, norm(d) if isinstance(d, str) else d) for k, d in c) for _, c, _ in resolved}
            mode_name = mode.split(".")[-1] if mode else None
            if all(not c for _, c, _ in resolved) and mode_name not in ("ONCE_PER_BLOCK", "PER_BLOCKER"):
                continue
            if len(chains) != 1:
                raise Keep("its effects carry different wrappers")
            chain = resolved[0][1]
            kinds = [k for k, _ in chain]
            if [ORDER[k] for k in kinds] != sorted(ORDER[k] for k in kinds) or len(set(ORDER[k] for k in kinds)) != len(kinds):
                if kinds.count("I") == 2 and kinds[-1] == "I" and kinds[-2] == "I":
                    pass  # an effect-text "if" inside the intervening one: the inner one stays an effect
                else:
                    raise Keep("wrappers in an order the legacy view doesn't rebuild: " + ">".join(kinds))
            limit, predicate, condition, multiplicity, binding = None, None, None, None, None
            inner_wrap = []  # wrappers left on the effect, innermost last
            seen_i = False
            for kind, data in chain:
                if kind == "L":
                    limit = data
                elif kind == "O":
                    if limit is not None:
                        raise Keep("two limits")
                    limit = "ONCE"
                elif kind == "P":
                    predicate = data
                elif kind == "C":
                    if trig != "creatureYouControlAttacks":
                        raise Keep("a card filter on %s (its collector judges the card)" % trig)
                    predicate = do.card_to_permanent(data)
                    needs.update(re.findall(r"new (Permanent\w+Predicate)\(", predicate))
                    if "Set.of(" in predicate:
                        needs.add("Set")
                    if "List.of(" in predicate:
                        needs.add("List")
                elif kind == "A":
                    if trig not in ALLY_DAMAGE:
                        raise Keep("an AllyCombatDamageTriggerEffect on " + trig)
                    pred, bind, once, opponents = data
                    if bare(pred) != "null":
                        if predicate is not None:
                            raise Keep("two dealer filters")
                        predicate = pred
                    if once:
                        multiplicity = "ONE_OR_MORE"
                    if bind and opponents:
                        raise Keep("an ally combat damage record bound to the dealer and to opponents")
                    binding = "boundToDealer" if bind else "opponentsDealtDamage" if opponents else None
                elif kind == "B":
                    if trig != "creatureDealsCombatDamageToYou":
                        raise Keep("a batched wrapper on " + trig)
                    multiplicity = "ONE_OR_MORE"
                elif kind == "I":
                    if seen_i:
                        inner_wrap.append(("I", data))
                        continue
                    seen_i = True
                    if kept(NOT_INTERVENING, name, data):
                        inner_wrap.append(("I", data))
                        continue
                    cls = condition_class(data)
                    intervening = (cls in QUALIFIERS or name in INTERVENING_CARDS
                                   or len(intervening_sentences(oracle)) >= counts.get("I", 0))
                    if not intervening:
                        report.append("UNDECIDED\t%s\t%s\t%s" % (name, text, oracle.replace("\n", " | ")))
                        raise Keep("undecided")
                    condition = data
            if limit == "ONCE" and any(k == "I" for k, _ in inner_wrap):
                raise Keep("\"triggers only once\" inside an effect-text if")
            if mode_name == "ONCE_PER_BLOCK":
                if trig not in BLOCK_ONE_OR_MORE or multiplicity:
                    raise Keep("ONCE_PER_BLOCK on " + trig)
                multiplicity, mode = "ONE_OR_MORE", None
            elif mode_name == "PER_BLOCKER":
                if trig not in BECOMES_BLOCKED or multiplicity:
                    raise Keep("PER_BLOCKER on " + trig)
                multiplicity, mode = "EACH_BLOCKER", None
            elif mode_name not in (None, "INDEPENDENT", "NORMAL"):
                raise Keep("trigger mode " + mode_name)
            if m.group(0) == "addSimultaneousTrigger(" and mode is not None:
                raise Keep("a simultaneous ability with a trigger mode")
        except Keep as keep:
            if str(keep) != "undecided":
                report.append("KEEP\t%s\t%s\t%s" % (name, keep, text))
            continue

        if (limit is None and predicate is None and condition is None and multiplicity is None and binding is None
                and not any(k in ("A", "B", "P", "C") for k, _ in chain)):
            report.append("KEEP\t%s\tno trigger-level wrapper (an effect-text if)\t%s" % (name, text))
            continue
        # Build the new statement.
        trigger = args[0]
        if predicate is not None:
            try:
                trigger = with_filter(trigger, predicate, needs)
            except Keep as keep:
                report.append("KEEP\t%s\t%s\t%s" % (name, keep, text))
                continue
        line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(line) - len(line.lstrip()) + 8)
        trigger = break_filter(trigger, len(line) + len(m.group(0)), indent)
        base = {"ONE_OR_MORE": "TriggerOptions.ONE_OR_MORE", "EACH_BLOCKER": "TriggerOptions.EACH_BLOCKER",
                None: "TriggerOptions.DEFAULT"}[multiplicity]
        opts = []
        if condition is not None or limit or multiplicity or options is not None:
            needs.add("TriggerOptions")
            if limit:
                needs.add("TriggerLimit")
            opts = [options_expr(base, condition, limit, indent)]
        if m.group(0) == "new TriggeredAbility(" and mode is not None:
            report.append("KEEP\t%s\ta granted ability with a trigger mode\t%s" % (name, text))
            continue
        new_effects = []
        for (variable, _, inner), written in zip(resolved, effects):
            body = inner
            for kind, data in reversed(inner_wrap):
                body = "new ConditionalEffect(%s, %s)" % (data, body)
            if binding:
                needs.add("AllyCombatDamageTriggerEffect")
                body = "AllyCombatDamageTriggerEffect.%s(%s)" % (binding, body)
            if variable is not None:
                vname, (vstart, vend, vtext) = variable
                if norm(body) != norm(vtext):
                    var_edits[vname] = (vstart, vend, body)
                new_effects.append(vname)
            else:
                new_effects.append(body)
        call_args = [trigger] + ([mode] if mode else []) + opts + new_effects
        body = do.layout(call_args, indent, len(line) + len(m.group(0)))
        edits.append((m.start(), end, m.group(0) + body + ")"))
        spans.append((m.start(), end))
        kinds = "".join(k for k, _ in chain) + ("/" + mode_name if mode_name else "")
        report.append("CONVERT\t%s\t%s\t%s" % (name, kinds, text))
    return edits, var_edits, spans, needs


def convert_predicate_variables(src, needs):
    """Converts the definitions of the card-predicate variables a card filter named (do.PREDICATE_VARIABLES)."""
    for vname in sorted(do.PREDICATE_VARIABLES):
        found = do.definition(src, vname)
        if found is None:
            raise ValueError("no one definition of the card predicate " + vname)
        vstart, vend, text = found
        uses = [u for u in re.finditer(r"\b%s\b" % re.escape(vname), src)
                if not (vstart <= u.start() < vend) and not re.match(r"\s*=", src[u.end():])]
        if any(".withFilter(" + vname not in src[max(0, u.start() - 12):u.end()] for u in uses):
            raise ValueError("the card predicate %s is also used outside a combat filter" % vname)
        converted = do.card_to_permanent(text)
        needs.update(re.findall(r"new (Permanent\w+Predicate)\(", converted))
        if "Set.of(" in converted:
            needs.add("Set")
        src = src[:vstart] + converted + src[vend:]
        declared = re.search(r"\b([A-Z]\w*)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        if declared and declared.group(1) != "var":
            src = src[:declared.start(1)] + "PermanentPredicate" + src[declared.end(1):]
            needs.add("PermanentPredicate")
    do.PREDICATE_VARIABLES.clear()
    return src


def count_wrapped(src):
    """How many combat abilities of the card carry an intervening-if candidate."""
    n = 0
    for m, end, args, mode, options, effects in abilities(src):
        try:
            resolved = resolve(effects, src)
        except Keep:
            continue
        if any(any(k == "I" for k, _ in c) for _, c, _ in resolved):
            n += 1
    return n


def convert(src, name, oracle, report):
    do.PREDICATE_VARIABLES.clear()
    counts = {"I": count_wrapped(src)}
    edits, var_edits, spans, needs = decide(name, src, oracle, report, counts)
    for vname, (vstart, vend, _) in var_edits.items():
        for use in re.finditer(r"\b%s\b" % re.escape(vname), src):
            if vstart <= use.start() < vend or re.match(r"\s*=", src[use.end():]):
                continue
            if not any(a <= use.start() < b for a, b in spans):
                raise ValueError("%s is also used outside the converted abilities" % vname)
    for vname, (vstart, vend, text) in var_edits.items():
        edits.append((vstart, vend, text))
        declared = re.search(r"\b([A-Z]\w*)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        if declared and declared.group(1) not in ("CardEffect", "var"):
            edits.append((declared.start(1), declared.end(1), "CardEffect"))
            needs.add("CardEffect")
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    src = convert_predicate_variables(src, needs)
    return src, needs


STATEMENT = re.compile(r"^([ \t]*)((?:[a-z]\w*\.)?)addTrigger\(", re.M)


def merge(src, name, oracle, report):
    """Merges each "attacks or blocks" pair into one ability with both trigger conditions."""
    if not re.search(r"\battacks or blocks\b", oracle, re.I):
        return src, set()
    found = []
    for m in STATEMENT.finditer(src):
        end = po.close_paren(src, m.end())
        if not src[end:].startswith(";"):
            continue
        args = po.split_args(src[m.end():end - 1])
        trig = norm(bare(args[0]))
        if trig not in ("Triggers.attacks()", "Triggers.blocks()"):
            continue
        rest = args[1:]
        options = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerOptions\b", rest[0]) else None
        if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]):
            continue
        found.append((m, end, trig, options, rest))
    attacks = [f for f in found if f[2] == "Triggers.attacks()"]
    blocks = [f for f in found if f[2] == "Triggers.blocks()"]
    edits, needs, used = [], set(), set()
    for a in attacks:
        for b in blocks:
            if id(b) in used or a[0].group(2) != b[0].group(2):
                continue
            if norm(",".join(a[4])) != norm(",".join(b[4])):
                continue
            oa = norm(bare(a[3])) if a[3] else "TriggerOptions.DEFAULT"
            ob = norm(bare(b[3])) if b[3] else "TriggerOptions.DEFAULT"
            if oa == ob:
                options = a[3]
            elif oa == "TriggerOptions.DEFAULT" and ob.startswith("TriggerOptions.ONE_OR_MORE"):
                options = b[3]
            else:
                continue
            used.add(id(b))
            m = a[0]
            indent = m.group(1) + " " * 8
            call_args = ["List.of(Triggers.attacks(), Triggers.blocks())"] + ([options] if options else []) + a[4]
            body = do.layout(call_args, indent, len(m.group(1)) + len(m.group(2)) + len("addTrigger("))
            edits.append((m.start(2), a[1], m.group(2) + "addTrigger(" + body + ")"))
            # Drop the blocks statement with its line (and a comment line right above it).
            bstart = src.rfind("\n", 0, b[0].start()) + 1
            prev = src.rfind("\n", 0, bstart - 1) + 1
            if src[prev:bstart].strip().startswith("//"):
                bstart = prev
            bend = src.find("\n", b[1]) + 1
            edits.append((bstart, bend, ""))
            needs.add("List")
            report.append("MERGE\t%s\t%s" % (name, re.sub(r"\s+", " ", src[a[0].start():a[1]])))
            break
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    return src, needs


def main():
    dry_run = "--dry-run" in sys.argv
    show_report = "--report" in sys.argv
    changed, errors, report = 0, [], []
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                if not FAMILY.search(original):
                    continue
                oracle = pilot.oracle(pilot.REG.findall(original)) or ""
                try:
                    src, needs = convert(original, name, oracle, report)
                    src, merged = merge(src, name, oracle, report)
                    needs |= merged
                except ValueError as error:
                    errors.append("%s: %s" % (path, error))
                    continue
                if src == original:
                    continue
                src = add_imports(src, [IMPORTS[n] for n in sorted(needs)])
                src = po.drop_unused_imports(original, src)
                changed += 1
                if dry_run:
                    if not show_report:
                        sys.stdout.writelines(difflib.unified_diff(
                            original.splitlines(True), src.splitlines(True), path, path))
                else:
                    open(path, "w").write(src)
    if show_report:
        print("\n".join(report))
    undecided = [r for r in report if r.startswith("UNDECIDED")]
    print("%d card classes %s, %d undecided" % (changed, "would change" if dry_run else "changed", len(undecided)),
          file=sys.stderr)
    if errors:
        raise SystemExit("\n".join(errors))
    if undecided and not dry_run:
        raise SystemExit("undecided abilities (add them to INTERVENING_CARDS, NOT_INTERVENING or KEEP)")


if __name__ == "__main__":
    main()
