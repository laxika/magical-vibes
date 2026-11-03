#!/usr/bin/env python3
"""Step 14, run 4b of effect-slot-refactor.md: the intervening "if"s (CR 603.4) of the source's own "when this enters"
abilities (Triggers.enters(ObjectRelation.THIS), collected by EtbTriggerService) become TriggerOptions.

An own enters ability whose every effect is `new ConditionalEffect(c, x)` / `(c, x, true)` with one condition c, the
effect written out or passed as a local variable with one definition, becomes

    addTrigger(Triggers.enters(ObjectRelation.THIS)[, TriggerMode.X], TriggerOptions.DEFAULT.withInterveningIf(c), x…)

on the card or its target group. The enters collector reads the ability through TriggeredAbility.legacyEffects(), which
wraps each effect in the same ConditionalEffect again (run 4a), so nothing it sees changes.

Which wrappers are intervening "if"s is decided by the Oracle text: the card's text has an "When … enters, if …" ability,
or the card is in KEYWORD (a keyword whose rules define the ability with an intervening "if", or a trigger condition on
how the permanent entered). In UNLESS, the wrapper is the effect text's own "If …" sentence, checked as the ability
resolves (no intervening "if"), and the enters collector doesn't look at its condition, so it becomes
`ConditionalEffect.unless(c, x)`, which the collector and the resolution treat the same way. KEEP lists the cards whose
wrapper is also no intervening "if" but whose condition the collector reads as the ability triggers (a cast-time fact,
so it makes no difference when it's checked): they stay for run 16. SKIP keeps a card for another reason. Any other
wrapped own enters ability stops the script, so no card is decided silently.

--dry-run prints the diffs. Usage: python3 scripts/step14-own-enters-options.py [--dry-run] <module dir>...
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
OWN = "Triggers.enters(ObjectRelation.THIS)"
INTERVENING = re.compile(r"\b(?:When|Whenever)\b[^.\n|]*?\benters?\b[^.\n|]*?,\s*if\b", re.I)

KEYWORD = {
    # Ravenous (CR 702.156a): "When this permanent enters, if X is 5 or more, draw a card."
    "Aberrant.java", "Broodlord.java", "Exocrine.java", "HormagauntHorde.java", "JackedRabbit.java", "Mawloc.java",
    "Ravener.java", "Sporocyst.java", "TermagantSwarm.java", "Tervigon.java", "TyrantGuard.java", "Zoanthrope.java",
    # Offspring (CR 702.175a): "When this permanent enters, if its offspring cost was paid, create a token …"
    "AgateInstigator.java", "BushyBodyguard.java", "CoruscationMage.java", "DarkstarAugur.java",
    "FinchFormation.java", "FlowerfootSwordmaster.java", "FountainportCharmer.java", "IntrepidRabbit.java",
    "IridescentVinelasher.java", "ManifoldMouse.java", "PawpatchRecruit.java", "ProsperousBandit.java",
    "RustShieldRampager.java", "SplashLasher.java", "StarscapeCleric.java", "SteampathCharger.java",
    "SteelburrChampion.java", "TenderWildguide.java", "ThornplateIntimidator.java", "ThundertrapTrainer.java",
    "WarrenWarleader.java",
    # Squad (CR 702.157a): "When this creature enters, if its squad cost was paid, create a token …"
    "ArcoFlagellant.java", "RoadkillRodney.java", "RuthlessRadrat.java", "ThrillKillDisciple.java",
    "UltramarinesHonourGuard.java",
    # Gift (CR 702.174b): "When this permanent enters, if its gift cost was paid, [effect]."
    "Octomancer.java",
    # Blitz (CR 702.152a): no triggered ability ("As long as this permanent's blitz cost was paid, it has haste",
    # and a sacrifice at the next end step); the collector drops these as it always did (StackResolutionService
    # applies blitz) and the option only marks them, as the wrapper did.
    "EffluenceDevourer.java", "JaxisTheTroublemaker.java", "MezzioMugger.java", "NightClubber.java",
    "PlasmaJockey.java", "RiveteersDecoy.java", "RiveteersProvocateur.java", "SabinMasterMonk.java",
    "StarAthlete.java", "TenaciousUnderdog.java", "WaveOfRats.java",
    # "When this land enters untapped", "When it enters this way" (escape), "When this enters during the declare
    # attackers step": a condition on how the permanent entered, checked as it triggers (the collector reads no
    # filter on the source's own entry, run 4a).
    "DwarvenMine.java", "GingerbreadCabin.java", "IdyllicGrange.java", "MysticSanctuary.java", "WitchsCottage.java",
    "PharikasSpawn.java", "MisleadingSignpost.java", "PortalMage.java",
}

UNLESS = {
    # "If you control a creature with a counter on it, the chosen creature doesn't untap …"
    "CelestialRegulator.java",
    # "draw a card if you've cast another blue spell this turn"
    "DreamThief.java",
    # "If an opponent controls that creature, put a stun counter on it."
    "FearOfImmobility.java",
    # "If you controlled it, investigate."
    "HotshotInvestigators.java",
    # "scry 3, then reveal the top card of your library. If it's a land or double-faced card, draw a card."
    "InvasionOfPyrulea.java",
    # "then you may have this creature fight that creature" (the target's restriction, checked as it resolves)
    "KraulHarpooner.java",
    # "If that creature's power is 2 or less or if you control another Lizard, gain control …"
    "ReptilianRecruiter.java",
    # "choose one —": the mode chosen as it entered
    "AinokGuide.java",
    # "If you weren't the starting player, you may untap Forsaken Crossroads instead."
    "ForsakenCrossroads.java",
    # "exile target creature an opponent controls until this creature leaves the battlefield" (CR 610.3b)
    "RasaadMonkOfSelNe.java",
}

KEEP = {
    # "… instead" / "Then if …" sentences on a kicker, alternative cost, gift, spectacle, entry zone, snow or X: the
    # collector reads these cast-time facts as the ability triggers (an intervening "if" in effect, the same result).
    "AdiposeOffspring.java", "AllStarKicker.java", "BergStrider.java", "DesolationAngel.java",
    "DesolationGiant.java", "DunbarrowRevivalist.java", "FblthpTheLost.java", "GrimReapersSprint.java",
    "KineticOoze.java", "Kitnap.java", "RixMaadiReveler.java", "SeaGateStormcaller.java", "SphinxOfLostTruths.java",
    "TurncoatKunoichi.java",
    # "If you do or if you control another Dinosaur, you gain 3 life": no intervening "if", but the collector checks
    # ControlsAnotherPermanent as it triggers (an ETB gate), so `unless` would change when it's checked; run 4c.
    "ArmoredKincaller.java",
}

SKIP = {
    # "Whenever a [creature / artifact] enters" on a card that isn't always one: the own half's "if it's a
    # [creature]" is a filter on the source's own entry, which EtbTriggerService can't read yet (run 4a's list).
    "AetherFlash.java", "CarnivalOfSouls.java", "PathOfDiscovery.java", "WeldfastWingsmith.java",
    # "Whenever this enters or attacks, if …": the wrapped effect is shared with an attacks ability, whose collector
    # doesn't read options yet (step 13 family 3, run 7, merges the two into one ability).
    "BloodhallPriest.java", "NaomiPillarOfOrder.java", "OmnivorousFlytrap.java",
    # Its "if it's a creature" ability is also granted to a token copy through a TriggeredAbility the conversion
    # doesn't follow.
    "MythosOfIlluna.java",
}

IMPORTS = {
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "CardEffect": "com.github.laxika.magicalvibes.model.effect.CardEffect",
}


def conditional(effect):
    """(condition, wrapped) of a default intervening-"if" ConditionalEffect expression, else None."""
    args = po.call(do.strip_fq(effect), "new ConditionalEffect")
    if args is not None and (len(args) == 2 or (len(args) == 3 and args[2] == "true")):
        return args[0], args[1]
    return None


OPTIONS = "TriggerOptions.DEFAULT.withInterveningIf("


def options_expr(condition, indent, column):
    """The intervening-"if" option, its condition on the next line when the option doesn't fit in 120 columns, keeping
    the condition's own line breaks (it started at {@code column}) when it doesn't fit on one."""
    flat = do.collapse(condition)
    if len(indent) + len(OPTIONS) + len(flat) + 2 <= 120:
        return OPTIONS + flat + ")"
    inner = len(indent) + 8
    if inner + len(flat) + 2 <= 120 or "\n" not in condition.strip():
        return OPTIONS + "\n" + " " * inner + flat + ")"
    lines = condition.strip().split("\n")
    shifted = [" " * max(0, len(l) - len(l.lstrip()) - column + inner) + l.lstrip() if l.strip() else ""
               for l in lines[1:]]
    return OPTIONS + "\n" + " " * inner + "\n".join([lines[0].rstrip()] + shifted) + ")"


def norm(text):
    return re.sub(r"\s+", "", text)


def own_abilities(src):
    """Yields (match, end, args, mode, effects) for each own enters ability of the source."""
    for m in ABILITY.finditer(src):
        end = po.close_paren(src, m.end())
        args = po.split_args(src[m.end():end - 1])
        if norm(do.strip_fq(args[0])) != norm(OWN):
            continue
        rest = args[1:]
        mode = rest.pop(0) if rest and re.match(r"(?:[\w.]+\.)?TriggerMode\.\w+$", rest[0]) else None
        yield m, end, args, mode, rest


def convert(src, name, oracle):
    """Converts the card's wrapped own enters abilities; returns the new source and the simple names it needs."""
    needs, edits, var_edits, spans = set(), [], {}, []
    intervening = name in KEYWORD or bool(INTERVENING.search(oracle))
    for m, end, args, mode, effects in own_abilities(src):
        if not effects or any(re.match(r"(?:[\w.]+\.)?TriggerOptions\b", e) for e in effects):
            continue
        resolved = []
        for effect in effects:
            variable = None
            if re.fullmatch(r"[a-z]\w*", effect.strip()):
                found = do.definition(src, effect.strip())
                if found is None:
                    break
                variable = (effect.strip(), found)
                effect = found[2]
            parts = conditional(effect)
            if parts is None:
                break
            resolved.append((variable, parts))
        else:
            conditions = {norm(cond) for _, (cond, _) in resolved}
            if len(conditions) != 1:
                raise ValueError("an own enters ability with two conditions: " + src[m.start():end])
            if name in SKIP or name in KEEP:
                continue
            if m.group(0) != "addTrigger(":
                raise ValueError("a simultaneous wrapped ability: " + src[m.start():end])
            if name in UNLESS:
                for variable, _ in resolved:
                    if variable is not None:
                        raise ValueError("an unless wrapper in a variable: " + variable[0])
                body = src[m.start():end]
                edits.append((m.start(), end, re.sub(r"new ConditionalEffect\(", "ConditionalEffect.unless(", body)))
                continue
            if not intervening:
                raise ValueError("a wrapped own enters ability without an intervening \"if\" in its Oracle text "
                                 "(add the card to KEYWORD, UNLESS, KEEP or SKIP): " + src[m.start():end])
            condition = resolved[0][1][0]
            inner_effects = []
            for (variable, (_, inner)), written in zip(resolved, effects):
                if variable is not None:
                    vname, (vstart, vend, _) = variable
                    var_edits[vname] = (vstart, vend, inner)
                    inner_effects.append(vname)
                else:
                    inner_effects.append(inner)
            needs.add("TriggerOptions")
            line = re.search(r"[^\n]*$", src[:m.start()]).group(0)
            indent = " " * (len(line) - len(line.lstrip()) + 8)
            where = src.find(condition.strip(), m.start())
            if where < 0:
                where = src.find(condition.strip())
            column = where - src.rfind("\n", 0, where) - 1
            options = options_expr(condition, indent, column)
            body = do.layout([args[0]] + ([mode] if mode else []) + [options] + inner_effects, indent,
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
        if declared and declared.group(1) == "ConditionalEffect":
            edits.append((declared.start(1), declared.end(1), "CardEffect"))
            needs.add("CardEffect")
    for start, end, text in sorted(edits, reverse=True):
        src = src[:start] + text + src[end:]
    return src, needs


def main():
    dry_run = "--dry-run" in sys.argv
    changed, errors = 0, []
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root or "/test/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                if "ConditionalEffect" not in original or OWN not in original:
                    continue
                oracle = pilot.oracle(pilot.REG.findall(original)) or ""
                try:
                    src, needs = convert(original, name, oracle)
                except ValueError as error:
                    errors.append("%s: %s" % (path, error))
                    continue
                if src == original:
                    continue
                src = po.add_imports(src, [IMPORTS[n] for n in sorted(needs)])
                src = po.drop_unused_imports(original, src)
                changed += 1
                if dry_run:
                    sys.stdout.writelines(difflib.unified_diff(
                        original.splitlines(True), src.splitlines(True), path, path))
                else:
                    open(path, "w").write(src)
                    print(path)
    print("%d card classes %s" % (changed, "would change" if dry_run else "changed"), file=sys.stderr)
    if errors:
        raise SystemExit("\n".join(errors))


if __name__ == "__main__":
    main()
