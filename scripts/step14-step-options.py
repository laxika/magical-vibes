#!/usr/bin/env python3
"""Step 14, run 5 of effect-slot-refactor.md: the trigger-only wrappers of the "at the beginning of [step or phase]"
abilities (Triggers.upkeep / drawStep / precombatMainPhase / beginningOfCombat / endOfCombat / postcombatMainPhase /
secondMainPhase / endStep, collected by StepTriggerService) become TriggerOptions.

A step ability whose every effect is `new ConditionalEffect(c, x)` / `(c, x, true)` with one condition c, the effect
written out or passed as a local variable with one definition, becomes

    addTrigger(<trigger>[, TriggerMode.X], TriggerOptions.DEFAULT.withInterveningIf(c), x…)

on the card or its target group (a `new TriggerCondition(…)` ability's `TriggerOptions.DEFAULT` gains the option). The
step collector reads the ability through TriggeredAbility.legacyEffects(), which wraps each effect in the same
ConditionalEffect again, so nothing it sees changes. Also:

- "This ability triggers only once" (`new OnceOnlyTriggerEffect(x)` under the intervening "if") is the limit
  `TriggerLimit.ONCE`, which the legacy view gives the collector as the same wrapper.
- Survival (`new SurvivalTriggerEffect(new ConditionalEffect(SourceIsTapped, x))` on postcombatMainPhase) and every
  other "at the beginning of your second main phase" ability on postcombatMainPhase is `Triggers.secondMainPhase`, the
  turn's second main phase (CR 505.1).
- Curse of Obsession's and Tenuous Truce's `TargetPlayerIsActive` wrapper is always true on
  `endStep(ENCHANTED_PLAYER)` and is dropped.

Which wrappers are intervening "if"s is decided by the Oracle text: the card's text has an "At … , if" ability (CR
603.4), or the card is in INTERVENING. NOT_INTERVENING lists the cards whose wrapper is the effect text's own "If …"
sentence, kept for run 16 (the step collectors check a wrapper's condition as the ability triggers whichever flag it
has, so `unless` would change behaviour). Any other wrapped step ability stops the script, so no card is decided
silently.

--dry-run prints the diffs. Usage: python3 scripts/step14-step-options.py [--dry-run] <module dir>...
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

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(")
STEP = re.compile(r"(?:new TriggerCondition\(\s*)?Triggers\.(upkeep|drawStep|precombatMainPhase|beginningOfCombat|"
                  r"endOfCombat|postcombatMainPhase|secondMainPhase|endStep)\(")
SECOND_MAIN = re.compile(r"\bAt the beginning of your second main phase\b", re.I)
# The Oracle words of each step trigger, for its "At [the beginning of …] [step], if" sentence (CR 603.4).
STEP_WORDS = {
    "upkeep": r"upkeep",
    "drawStep": r"draw step",
    "precombatMainPhase": r"(?:first |precombat )?main phase",
    "beginningOfCombat": r"(?:beginning of combat|combat on|each combat)",
    "endOfCombat": r"end of combat",
    "postcombatMainPhase": r"(?:second |postcombat )?main phase",
    "secondMainPhase": r"second main phase",
    "endStep": r"end step",
}

# Cards whose wrapped step abilities are all intervening "if"s though the Oracle text doesn't say "At …, if" once
# for each of them:
INTERVENING_CARDS = {
    # One Oracle ability split over two abilities (two zones, or "each player's upkeep" as YOU and OPPONENT halves,
    # or two effects), each with the same intervening "if".
    "ArvadWeatherlightSmuggler.java", "AsylumVisitor.java", "KjeldoranHomeGuard.java",
    # Vanishing (CR 702.63a): "At the beginning of your upkeep, if this permanent has a time counter on it, remove a
    # time counter from it."
    "AvenRiftwatcher.java", "Calciderm.java", "Chronozoa.java", "RegenerationsRestored.java", "SoultetherGolem.java",
    # Impending (CR 702.176a): "At the beginning of your end step, if this permanent's impending cost was paid and it
    # has a time counter on it, remove a time counter from it."
    "LurkerInTheDeep.java", "OverlordOfTheBalemurk.java", "OverlordOfTheBoilerbilges.java",
    "OverlordOfTheFloodpits.java", "OverlordOfTheHauntwoods.java", "OverlordOfTheMistmoors.java",
    # "To solve — [condition]" (CR 719.3a): "At the beginning of your end step, if [condition] and this Case is not
    # solved, this Case becomes solved"; and a Case's "Solved —" step ability (CR 719.3c).
    "CaseOfTheBurningMasks.java", "CaseOfTheCrimsonPulse.java", "CaseOfTheFilchedFalcon.java",
    "CaseOfTheGatewayExpress.java", "CaseOfTheGorgonsKiss.java", "CaseOfTheLockedHothouse.java",
    "CaseOfTheLostWitness.java", "CaseOfTheMarketMelee.java", "CaseOfThePilferedProof.java",
    "CaseOfTheRansackedLab.java", "CaseOfTheShatteredPact.java", "CaseOfTheShiftingVisage.java",
    "CaseOfTheStashedSkeleton.java", "CaseOfTheTrampledGarden.java", "CaseOfTheUneatenFeast.java",
    # An ability the permanent has only at a Class level, in a chosen mode, with a door unlocked, at max speed or
    # harnessed: the condition is an option, as runs 2 and 3a made the Class levels and chosen modes of their families.
    "BanditsTalent.java", "BlacksmithsTalent.java", "DoesMachines.java", "MonkClass.java", "ScavengersTalent.java",
    "WarlockClass.java", "BarrensteppeSiege.java", "BattleOfHooverDam.java", "CitadelSiege.java",
    "FrontierSiege.java", "MirrodinBesieged.java", "MonasterySiege.java", "OutpostSiege.java", "PalaceSiege.java",
    "PhenomenonInvestigators.java", "StruggleForProjectPurity.java", "WindcragSiege.java",
    "GlassworksShatteredYard.java", "RoaringFurnaceSteamingSauna.java", "SmokyLoungeMistySalon.java",
    "HangarbackAssembler.java", "RisingChicane.java", "SmogSmasher.java", "TheMindStone.java", "TheSoulStone.java",
    # Back faces (no Oracle text of their own in the cache) and day/night faces: "At the beginning of each upkeep, if
    # [no spells / a player cast two or more spells] last turn, transform [this]", "At the beginning of your end step,
    # if [this] didn't attack this turn, …", "if you gained life this turn", "if you haven't been dealt combat damage
    # since your last turn".
    "BaneOfHanweir.java", "BloodsoakedReveler.java", "BrandedHowler.java", "FlameheartWerewolf.java",
    "GatstafHowler.java", "GatstafRavagers.java", "HinterlandScourge.java", "HomicidalBrute.java",
    "HowlpackAlpha.java", "HowlpackOfEstwald.java", "Ironfang.java", "KrallenhordeHowler.java",
    "KrallenhordeKiller.java", "KrallenhordeWantons.java", "LambholtButcher.java", "LoneWolfOfTheNatterknolls.java",
    "MarchesaResoluteMonarch.java", "MercilessPredator.java", "MoonriseIntruder.java", "MoonscarredWerewolf.java",
    "NeckBreaker.java", "NightfallPredator.java", "OneOfThePack.java", "RampagingWerewolf.java",
    "RavagerOfTheFells.java", "SeafaringWerewolf.java", "SilverpeltWerewolf.java", "SuspiciousStowaway.java",
    "TerrorOfKruinPass.java", "TimberShredder.java", "TovolarsHuntmaster.java", "TovolarsMagehunter.java",
    "TovolarsPackleader.java", "UlrichUncontestedAlpha.java", "UlvenwaldPrimordials.java", "VildinPackAlpha.java",
    "WerewolfOfAncientHunger.java", "WerewolfRansacker.java", "WildbloodPack.java",
}

# Wrappers that are no intervening "if", kept for run 16 (the step collectors check a wrapper's condition as the
# ability triggers whatever its flag says, so `unless` would change when it's checked). Card → the conditions kept
# (substrings of the condition expression; empty: every wrapped step ability of the card).
NOT_INTERVENING = {
    # "destroy that creature if it didn't attack this turn"
    "Aggression.java": [],
    # "Whenever this creature attacks or blocks, remove a +1/+1 counter from it at end of combat": a delayed
    # trigger, written as an end-of-combat ability gated on having attacked or blocked.
    "ClockworkDragon.java": [], "ClockworkVorrac.java": [],
    # "At end of combat, destroy all creatures blocking or blocked by this creature": gated on having been in combat.
    "KjeldoranFrostbeast.java": [],
    # "draw a card if you've cast a spell from a graveyard …", "that player may draw a card if a player other than
    # you lost life this turn"
    "LaboratoryDrudge.java": [], "LudevicNecroAlchemist.java": [],
    # "After each opponent's first combat phase of each turn, there is an additional combat phase."
    "ThroatWolf.java": [],
    # "If it isn't a creature, it becomes a 0/0 Robot artifact creature" (its Level 3 ability's own condition is an
    # option)
    "DoesMachines.java": ["TargetPermanentMatches"],
}

SKIP = {}

IMPORTS = {
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "TriggerLimit": "com.github.laxika.magicalvibes.model.trigger.TriggerLimit",
    "CardEffect": "com.github.laxika.magicalvibes.model.effect.CardEffect",
}


def conditional(effect):
    """(condition, wrapped) of a default intervening-"if" ConditionalEffect expression, else None."""
    args = po.call(do.strip_fq(effect), "new ConditionalEffect")
    if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
        return args[0], args[1]
    return None


def once_only(effect):
    """The wrapped effect of `new OnceOnlyTriggerEffect(x)`, else None."""
    args = po.call(do.strip_fq(effect), "new OnceOnlyTriggerEffect")
    return args[0] if args is not None and len(args) == 1 else None


def survival(effect):
    """The wrapped effect of `new SurvivalTriggerEffect(x)`, else None."""
    args = po.call(do.strip_fq(effect), "new SurvivalTriggerEffect")
    return args[0] if args is not None and len(args) == 1 else None


def norm(text):
    return re.sub(r"\s+", "", text)


def step_abilities(src):
    """Yields (match, end, args, mode, options, effects) for each step ability of the source."""
    for m in ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        if not STEP.match(do.strip_fq(args[0])):
            continue
        rest = args[1:]
        mode = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]) else None
        options = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerOptions\b", rest[0]) else None
        yield m, end, args, mode, options, rest


def options_expr(condition, limit, indent):
    """The ability's options, the condition on the next line when they don't fit in 120 columns."""
    tail = ".withLimit(TriggerLimit.%s)" % limit if limit else ""
    if condition is None:
        return "TriggerOptions.DEFAULT" + tail
    flat = do.collapse(condition)
    head = "TriggerOptions.DEFAULT.withInterveningIf("
    if len(indent) + len(head) + len(flat) + len(tail) + 2 <= 120:
        return head + flat + ")" + tail
    inner = indent + " " * 8
    if len(inner) + len(flat) + len(tail) + 2 <= 120:
        return head + "\n" + inner + flat + ")" + tail
    return head + "\n" + inner + do.reindent(condition, inner) + ")" + tail


def kept(name, condition):
    """Whether the wrapper on {@code condition} is no intervening "if" and stays (NOT_INTERVENING)."""
    if name not in NOT_INTERVENING:
        return False
    parts = NOT_INTERVENING[name]
    return not parts or any(part in condition for part in parts)


def step_kind(trigger):
    return STEP.match(do.strip_fq(trigger)).group(1)


def intervening_sentences(oracle, kind):
    """The card's "At …[step]…, if" sentences for a step trigger of {@code kind}."""
    return re.findall(r"\bAt\b[^.\n|]*?\b%s\b[^.\n|]*?,\s*if\b" % STEP_WORDS[kind], oracle, re.I)


def convert(src, name, oracle, report):
    """Converts the card's wrapped step abilities; returns the new source and the simple names it needs."""
    needs, edits, var_edits, spans = set(), [], {}, []
    candidates = []
    for m, end, args, mode, options, effects in step_abilities(src):
        if not effects or (options is not None and norm(do.strip_fq(options)) != "TriggerOptions.DEFAULT"):
            continue
        trigger = args[0]
        second = False
        resolved = []
        for effect in effects:
            variable = None
            if re.fullmatch(r"[a-z]\w*", effect.strip()):
                found = do.definition(src, effect.strip())
                if found is None:
                    break
                variable = (effect.strip(), found)
                effect = found[2]
            inner_survival = survival(effect)
            if inner_survival is not None:
                second = True
                effect = inner_survival
            parts = conditional(effect)
            if parts is None:
                break
            cond, inner = parts
            limit = None
            once = once_only(inner)
            if once is not None:
                limit, inner = "ONCE", once
            resolved.append((variable, cond, inner, limit))
        else:
            candidates.append((m, end, args, mode, options, effects, trigger, second, resolved))
    if not candidates:
        return src, needs
    per_kind = {}
    for candidate in candidates:
        if not kept(name, candidate[8][0][1]):
            kind = step_kind(candidate[6])
            per_kind[kind] = per_kind.get(kind, 0) + 1
    for m, end, args, mode, options, effects, trigger, second, resolved in candidates:
        body_text = src[m.start():end]
        conditions = {norm(cond) for _, cond, _, _ in resolved}
        limits = {limit for _, _, _, limit in resolved}
        if len(conditions) != 1 or len(limits) != 1:
            raise ValueError("a step ability with two conditions or limits: " + body_text)
        condition = resolved[0][1]
        if name in SKIP or kept(name, condition):
            report.append("KEEP\t%s\t%s" % (name, re.sub(r"\s+", " ", body_text)))
            continue
        kind = step_kind(trigger)
        intervening = name in INTERVENING_CARDS or len(intervening_sentences(oracle, kind)) >= per_kind[kind]
        drop = norm(condition) == "newTargetPlayerIsActive()"
        if not drop and not intervening:
            report.append("UNDECIDED\t%s\t%s\t%s" % (name, re.sub(r"\s+", " ", body_text),
                                                     oracle.replace("\n", " | ")))
            continue
        if m.group(0) != "addTrigger(":
            raise ValueError("a simultaneous wrapped ability: " + body_text)
        if second or (SECOND_MAIN.search(oracle) and "postcombatMainPhase" in trigger):
            if not SECOND_MAIN.search(oracle) or "postcombatMainPhase(ActivePlayer.YOU)" not in norm(trigger):
                raise ValueError("a Survival ability that isn't \"your second main phase\": " + body_text)
            trigger = trigger.replace("postcombatMainPhase", "secondMainPhase")
        inner_effects = []
        for (variable, _, inner, _), written in zip(resolved, effects):
            if variable is not None:
                vname, (vstart, vend, _) = variable
                var_edits[vname] = (vstart, vend, inner)
                inner_effects.append(vname)
            else:
                inner_effects.append(inner)
        limit = resolved[0][3]
        line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
        indent = " " * (len(line) - len(line.lstrip()) + 8)
        if drop and limit is None:
            opts = [] if options is None else [options]
        else:
            needs.add("TriggerOptions")
            if limit:
                needs.add("TriggerLimit")
            opts = [options_expr(None if drop else condition, limit, indent)]
        body = do.layout([trigger] + ([mode] if mode else []) + opts + inner_effects, indent,
                         len(line) + len(m.group(0)))
        edits.append((m.start(), end, m.group(0) + body + ")"))
        spans.append((m.start(), end))
        report.append("%s\t%s\t%s" % ("DROP" if drop else "OPTION", name, re.sub(r"\s+", " ", body_text)))
    for vname, (vstart, vend, _) in var_edits.items():
        for use in re.finditer(r"\b%s\b" % re.escape(vname), src):
            if vstart <= use.start() < vend or re.match(r"\s*=", src[use.end():]):
                continue
            if not any(a <= use.start() < b for a, b in spans):
                raise ValueError("%s is also used outside the converted abilities" % vname)
    for vname, (vstart, vend, text) in var_edits.items():
        edits.append((vstart, vend, text))
        declared = re.search(r"\b([A-Z]\w*)(\s+%s\s*=\s*)$" % re.escape(vname), src[:vstart])
        if declared and declared.group(1) in ("ConditionalEffect", "SurvivalTriggerEffect"):
            edits.append((declared.start(1), declared.end(1), "CardEffect"))
            needs.add("CardEffect")
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
                if "ConditionalEffect" not in original or not STEP.search(original):
                    continue
                oracle = pilot.oracle(pilot.REG.findall(original)) or ""
                try:
                    src, needs = convert(original, name, oracle, report)
                except ValueError as error:
                    errors.append("%s: %s" % (path, error))
                    continue
                if src == original:
                    continue
                src = po.add_imports(src, [IMPORTS[n] for n in sorted(needs)])
                src = po.drop_unused_imports(original, src)
                changed += 1
                if dry_run:
                    if not show_report:
                        sys.stdout.writelines(difflib.unified_diff(
                            original.splitlines(True), src.splitlines(True), path, path))
                else:
                    open(path, "w").write(src)
                    print(path)
    if show_report:
        print("\n".join(report))
    undecided = [r for r in report if r.startswith("UNDECIDED")]
    print("%d card classes %s, %d undecided" % (changed, "would change" if dry_run else "changed", len(undecided)),
          file=sys.stderr)
    if errors:
        raise SystemExit("\n".join(errors))
    if undecided and not dry_run:
        raise SystemExit("undecided abilities (add them to INTERVENING_CARDS, NOT_INTERVENING or SKIP)")


if __name__ == "__main__":
    main()
