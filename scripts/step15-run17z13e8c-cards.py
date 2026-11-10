#!/usr/bin/env python3
"""Step 15, run 17z13e8c: the cards that wrote one Oracle ability as several `addTrigger` calls on one step trigger.

The step readers read each triggered ability whole, as one stack object (CR 603.3), so an ability split into several
`addTrigger` calls (which the readers' merges used to join again) would become several stack objects. Each card below
was read against its Oracle text and is one ability; its calls become one `addTrigger` with every effect, in order
(a chained `target(...).addTrigger(...)` keeps its target group for every effect; an ability with two target groups,
Boros Battleshaper's and Betor, Ancestor's Voice's, binds each effect to its group with `bindEffect`). The cards whose
step abilities really are separate (Bloodhall Ooze, Corrosion, Dawn of the Dead, Illusionary Presence, Malignant
Growth, Palace Siege, Tangle Wire, Tetravus, Ashling, Rimebound, Barrensteppe Siege, Keeper of the Accord, Lunar
Convocation, Warlock Class) are not touched.

Usage: step15-run17z13e8c-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

BOC_YOU = r"Triggers\.beginningOfCombat\(ActivePlayer\.YOU\)"
BOC_ANY = r"Triggers\.beginningOfCombat\(ActivePlayer\.ANY\)"
UPKEEP = r"Triggers\.upkeep\(ActivePlayer\.YOU\)"


def chained(trigger):
    """`target(...).addTrigger(T, a).addTrigger(T, b)` -> `target(...).addTrigger(T, a, b)`."""
    return (re.compile(r"\)\s*\n(\s*)\.addTrigger\(" + trigger + r",\s*"), lambda m: ",\n" + m.group(1) + "        ")


def statements(trigger):
    """`addTrigger(T, a);\n addTrigger(T, b);` -> `addTrigger(T, a,\n b);`."""
    return (re.compile(r"\);\s*\n(\s*)addTrigger\(" + trigger + r",\s*"), lambda m: ",\n" + m.group(1) + "        ")


CHAINED_COMBAT = ["AdditiveEvolution", "AethershieldArtificer", "AvatarKyoshiEarthbender", "BaxterStockman",
                  "CombatProfessor", "DoctorDoomKingOfLatveria", "HalanaAndAlenaPartners", "ImotekhTheStormlord",
                  "JenovaAncientCalamity", "LivingLightningChargedUp", "MightOfTheAncestors", "MolecularModifier",
                  "NyxHerald", "PrairieSurvivalist", "PrimordialPlasm", "RecklessStormseeker",
                  "RosaResoluteWhiteMage", "SlimyDualleech", "TemmetVizierOfNaktamun", "XenagosGodOfRevels",
                  "AureliaExemplarOfJustice", "FireNationTurret", "HydaelynTheMothercrystal",
                  "SparkshaperVisionary", "StormChargedSlasher", "YouCannotHideFromMe"]
STATEMENT_COMBAT = ["HarriedDronesmith", "HowlsquadHeavy", "IrohDragonOfTheWest", "LagomosHandOfHatred",
                    "LegionWarboss", "MindwrackHarpy", "OldHobAlleycatBlues", "SereneSleuth",
                    "SightOfTheScalelords", "DeterminedIteration"]

# card -> (letter module, [(pattern, replacement)], the step trigger whose calls must end as one)
EDITS = {}
for name in CHAINED_COMBAT:
    EDITS[name] = (None, [chained(BOC_YOU)], BOC_YOU)
for name in STATEMENT_COMBAT:
    EDITS[name] = (None, [statements(BOC_YOU)], BOC_YOU)
EDITS["BolgEreborsReckoning"] = (None, [statements(BOC_ANY)], BOC_ANY)
EDITS["BloodgiftDemon"] = (None, [statements(UPKEEP)], UPKEEP)
EDITS["PlaneMergeElf"] = (None, [statements(UPKEEP)], UPKEEP)
EDITS["XanatharGuildKingpin"] = (None, [chained(UPKEEP)], UPKEEP)

KJELDORAN_OLD = """                new PutCountersOnSelfEffect(CounterType.MINUS_ZERO_MINUS_ONE));
        addTrigger(Triggers.endOfCombat(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceAttackedOrBlockedThisCombat()),
                DESERTER_TOKEN);"""
KJELDORAN_NEW = """                new PutCountersOnSelfEffect(CounterType.MINUS_ZERO_MINUS_ONE),
                DESERTER_TOKEN);"""
EDITS["KjeldoranHomeGuard"] = (None, [(KJELDORAN_OLD, KJELDORAN_NEW)], r"Triggers\.endOfCombat\(\)")

BOROS_OLD = """        // At the beginning of each combat, up to one target creature attacks or blocks this combat
        // if able and up to one target creature can't attack or block this combat.
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                        SequenceEffect.of(
                                new GrantStaticEffectToTargetUntilEndOfCombatEffect(new MustAttackEffect()),
                                new GrantStaticEffectToTargetUntilEndOfCombatEffect(new MustBlockEachCombatEffect())));

        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                        new LockTargetPermanentEffect(true, true, false, EffectDuration.UNTIL_END_OF_COMBAT));"""
BOROS_NEW = """        // At the beginning of each combat, up to one target creature attacks or blocks this combat
        // if able and up to one target creature can't attack or block this combat: one ability of two target groups.
        CardEffect attacksOrBlocks = SequenceEffect.of(
                new GrantStaticEffectToTargetUntilEndOfCombatEffect(new MustAttackEffect()),
                new GrantStaticEffectToTargetUntilEndOfCombatEffect(new MustBlockEachCombatEffect()));
        CardEffect cantAttackOrBlock = new LockTargetPermanentEffect(true, true, false,
                EffectDuration.UNTIL_END_OF_COMBAT);
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY), attacksOrBlocks, cantAttackOrBlock);
        target(TargetFilters.creature(), 0, 1).bindEffect(attacksOrBlocks);
        target(TargetFilters.creature(), 0, 1).bindEffect(cantAttackOrBlock);"""
EDITS["BorosBattleshaper"] = (None, [(BOROS_OLD, BOROS_NEW)], BOC_ANY)

BETOR_OLD = """        target(new ControlledPermanentPredicateTargetFilter(
                otherCreatureYouControl, "Target must be another creature you control"), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                new LifeGainedThisTurn(CountScope.CONTROLLER)));

        target(new GraveyardCardPredicateTargetFilter(new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .upTo(true)
                .maxManaValueEqualsLifeLostThisTurn(true)
                .build());"""
BETOR_NEW = """        // One ability of two target groups: the counters on up to one other target creature you control, the return
        // of up to one target creature card from your graveyard.
        CardEffect counters = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new LifeGainedThisTurn(CountScope.CONTROLLER));
        CardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .upTo(true)
                .maxManaValueEqualsLifeLostThisTurn(true)
                .build();
        addTrigger(Triggers.endStep(ActivePlayer.YOU), counters, returnCreature);
        target(new ControlledPermanentPredicateTargetFilter(
                otherCreatureYouControl, "Target must be another creature you control"), 0, 1)
                .bindEffect(counters);
        target(new GraveyardCardPredicateTargetFilter(new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .bindEffect(returnCreature);"""
EDITS["BetorAncestorsVoice"] = (None, [(BETOR_OLD, BETOR_NEW)], r"Triggers\.endStep\(ActivePlayer\.YOU\)")

CARD_EFFECT_IMPORT = "import com.github.laxika.magicalvibes.model.effect.CardEffect;\n"


def find(name):
    for module in ROOT.iterdir():
        if not (module / PKG).is_dir():
            continue
        for path in (module / PKG).rglob(f"{name}.java"):
            return module.name, path
    raise SystemExit(f"{name}: no class found")


def main():
    letters = set(sys.argv[1:])
    for name, (_, edits, trigger) in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        calls = len(re.findall(r"addTrigger\(\s*" + trigger, text))
        if calls == 1:
            continue
        new = text
        for pattern, replacement in edits:
            if isinstance(pattern, str):
                if pattern not in new:
                    raise SystemExit(f"{name}: edit didn't apply")
                new = new.replace(pattern, replacement)
            else:
                # Every call of the trigger but its first joins the call before it, which is the first's.
                first = re.search(r"addTrigger\(\s*" + trigger, new).start()
                parts, count, last = [], 0, 0
                for match in pattern.finditer(new):
                    if match.start(1) + len(match.group(1)) <= first:
                        continue
                    parts.append(new[last:match.start()])
                    parts.append(replacement(match))
                    last = match.end()
                    count += 1
                parts.append(new[last:])
                new = "".join(parts)
                if count == 0:
                    raise SystemExit(f"{name}: edit didn't apply")
        if "CardEffect " in new and CARD_EFFECT_IMPORT not in new:
            anchor = new.index("import com.github.laxika.magicalvibes.model.effect.")
            new = new[:anchor] + CARD_EFFECT_IMPORT + new[anchor:]
        if len(re.findall(r"addTrigger\(\s*" + trigger, new)) != 1:
            raise SystemExit(f"{name}: still more than one ability on {trigger}")
        path.write_text(new)
        print(f"merged {module}/{name}")


if __name__ == "__main__":
    main()
