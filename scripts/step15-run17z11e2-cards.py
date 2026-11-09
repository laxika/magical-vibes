#!/usr/bin/env python3
"""Step 15, run 17z11e2: the cards whose quoted granted step abilities the step collectors rebuilt or bound by their
effect's class are written as the abilities they grant (`GrantTriggeredAbilityEffect`).

- Necrotic Plague: "enchanted creature has 'At the beginning of your upkeep, sacrifice this creature.'" is a granted
  `Triggers.upkeep(YOU)` ability with `SacrificeSelfEffect`.
- Shifting Shadow: the granted upkeep ability is `DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect`.
- Withercrown: the granted "you lose 1 life unless you sacrifice this creature" is a `ForcedCostOrElseEffect` paid by
  sacrificing the source (`PermanentIsSourcePermanentPredicate`), as Town-Razer Tyrant's grant.
- Custody Battle: the granted "target opponent gains control of this creature unless you sacrifice a land" is a
  `ForcedCostOrElseEffect` with `TargetPlayerGainsControlOfSourceCreatureEffect`, the ability's own target filter
  "target opponent".
- Nettlevine Blight: the granted `Triggers.endStep(YOU)` ability with `SacrificeEnchantedPermanentAndReattachSourceAuraEffect`.
- Admiral Beckett Brass: "target nonland permanent controlled by a player who was dealt combat damage by three or more
  Pirates this turn" is the card's target filter with the new
  `PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate(PIRATE, 3)` and `GainControlOfTargetEffect(PERMANENT)`
  (the collector's class-keyed check and rebuilt target predicate are gone).
- The Energy Flux family (Energy Flux, Kataki, Aura Flux, Magus of the Tabernacle, Pendrell Mists, The Tabernacle at
  Pendrell Vale, Vile Consumption): "all [permanents] have 'At the beginning of your upkeep, sacrifice / destroy this
  [permanent] unless you pay …'" is a granted `Triggers.upkeep(YOU)` `ForcedCostOrElseEffect`; "all artifacts" grants
  it to the other artifacts and, while it is one, to the source itself (`ALL_PERMANENTS` and `SELF`), "all creatures"
  is `ALL_CREATURES_INCLUDING_SELF`, "other enchantments" `ALL_PERMANENTS`.

Each card's imports and constructor are written whole, so the script is idempotent. Per letter module.
Usage: step15-run17z11e2-cards.py [repository root]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"

M = "com.github.laxika.magicalvibes.model"
BASE = ["com.github.laxika.magicalvibes.cards.CardRegistration", f"{M}.Card"]
GRANT = [f"{M}.TriggeredAbility", f"{M}.effect.GrantScope", f"{M}.effect.GrantTriggeredAbilityEffect",
         f"{M}.trigger.ActivePlayer", f"{M}.trigger.Triggers"]


def flux(filter_import, filter_expr, cost_import, cost_expr, penalty_imports, penalty_expr, scopes, comment):
    imports = BASE + GRANT + [f"{M}.effect.ForcedCostOrElseEffect", cost_import, "java.util.List"] + penalty_imports
    if filter_import:
        imports.append(filter_import)
    lines = [f"        // {line}" for line in comment]
    lines.append("        TriggeredAbility payOrLose = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),")
    lines.append(f"                new ForcedCostOrElseEffect({cost_expr}, List.of({penalty_expr}), true));")
    for scope in scopes:
        if filter_expr:
            lines.append(f"        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.{scope},")
            lines.append(f"                {filter_expr}));")
        else:
            lines.append(f"        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.{scope}));")
    return imports, "\n".join(lines)


ARTIFACT = f"{M}.filter.PermanentIsArtifactPredicate"
ENCHANTMENT = f"{M}.filter.PermanentIsEnchantmentPredicate"
PAY_MANA = f"{M}.effect.PayManaCost"
SACRIFICE_SELF = [f"{M}.effect.SacrificeSelfEffect"]

CARDS_TO_WRITE = {
    ("e", "EnergyFlux"): flux(
        ARTIFACT, "new PermanentIsArtifactPredicate()", PAY_MANA, 'new PayManaCost("{2}")', SACRIFICE_SELF,
        "new SacrificeSelfEffect()", ["ALL_PERMANENTS", "SELF"],
        ['All artifacts have "At the beginning of your upkeep, sacrifice this artifact unless you pay {2}."']),
    ("k", "KatakiWarsWage"): flux(
        ARTIFACT, "new PermanentIsArtifactPredicate()", PAY_MANA, 'new PayManaCost("{1}")', SACRIFICE_SELF,
        "new SacrificeSelfEffect()", ["ALL_PERMANENTS", "SELF"],
        ['All artifacts have "At the beginning of your upkeep, sacrifice this artifact unless you pay {1}."']),
    ("a", "AuraFlux"): flux(
        ENCHANTMENT, "new PermanentIsEnchantmentPredicate()", PAY_MANA, 'new PayManaCost("{2}")', SACRIFICE_SELF,
        "new SacrificeSelfEffect()", ["ALL_PERMANENTS"],
        ['Other enchantments have "At the beginning of your upkeep, sacrifice this enchantment',
         'unless you pay {2}."']),
    ("m", "MagusOfTheTabernacle"): flux(
        None, None, PAY_MANA, 'new PayManaCost("{1}")', SACRIFICE_SELF, "new SacrificeSelfEffect()",
        ["ALL_CREATURES_INCLUDING_SELF"],
        ['All creatures have "At the beginning of your upkeep, sacrifice this creature unless you pay {1}."']),
    ("p", "PendrellMists"): flux(
        None, None, PAY_MANA, 'new PayManaCost("{1}")', SACRIFICE_SELF, "new SacrificeSelfEffect()",
        ["ALL_CREATURES_INCLUDING_SELF"],
        ['All creatures have "At the beginning of your upkeep, sacrifice this creature unless you pay {1}."']),
    ("t", "TheTabernacleAtPendrellVale"): flux(
        None, None, PAY_MANA, 'new PayManaCost("{1}")',
        [f"{M}.effect.DestroyReferencedPermanentEffect", f"{M}.effect.PermanentReference"],
        "new DestroyReferencedPermanentEffect(PermanentReference.SOURCE)", ["ALL_CREATURES_INCLUDING_SELF"],
        ['All creatures have "At the beginning of your upkeep, destroy this creature unless you pay {1}."']),
    ("v", "VileConsumption"): flux(
        None, None, f"{M}.effect.PayLifeCost", "new PayLifeCost(1)", SACRIFICE_SELF, "new SacrificeSelfEffect()",
        ["ALL_CREATURES_INCLUDING_SELF"],
        ['All creatures have "At the beginning of your upkeep, sacrifice this creature unless you pay 1 life."']),
    ("n", "NecroticPlague"): (
        BASE + GRANT + [f"{M}.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect",
                        f"{M}.effect.SacrificeSelfEffect", f"{M}.filter.TargetFilters",
                        f"{M}.trigger.ObjectRelation"],
        """        target(TargetFilters.creature())
                // Enchanted creature has "At the beginning of your upkeep, sacrifice this creature."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU), new SacrificeSelfEffect()),
                        GrantScope.ENCHANTED_CREATURE))

                // When enchanted creature dies, its controller chooses target creature one of their
                // opponents controls. Return Necrotic Plague from its owner's graveyard to the battlefield
                // attached to that creature.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToOpponentCreatureOnDeathEffect());"""),
    ("s", "ShiftingShadow"): (
        BASE + GRANT + [f"{M}.Keyword", f"{M}.effect.DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect",
                        f"{M}.effect.GrantKeywordEffect", f"{M}.filter.TargetFilters"],
        """        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature has "At the beginning of your upkeep, destroy this creature. Reveal cards from
                // the top of your library until you reveal a creature card. Put that card onto the battlefield and
                // attach Shifting Shadow to it, then put all other cards revealed this way on the bottom of your
                // library in a random order."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        new DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect()),
                        GrantScope.ENCHANTED_CREATURE));"""),
    ("w", "Withercrown"): (
        BASE + GRANT + [f"{M}.effect.ForcedCostOrElseEffect", f"{M}.effect.LoseLifeEffect",
                        f"{M}.effect.LoseLifeRecipient", f"{M}.effect.SacrificePermanentCost",
                        f"{M}.effect.SetBasePowerToughnessEffect",
                        f"{M}.filter.PermanentIsSourcePermanentPredicate", f"{M}.filter.TargetFilters",
                        "java.util.List"],
        """        target(TargetFilters.creature())
                .addStaticEffect(new SetBasePowerToughnessEffect(0, null, GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature has "At the beginning of your upkeep, you lose 1 life unless you sacrifice
                // this creature."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        new ForcedCostOrElseEffect(
                                new SacrificePermanentCost(new PermanentIsSourcePermanentPredicate(),
                                        "Sacrifice this creature", false),
                                List.of(new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER)),
                                true)),
                        GrantScope.ENCHANTED_CREATURE));"""),
    ("c", "CustodyBattle"): (
        BASE + GRANT + [f"{M}.effect.ForcedCostOrElseEffect", f"{M}.effect.SacrificePermanentCost",
                        f"{M}.effect.TargetPlayerGainsControlOfSourceCreatureEffect",
                        f"{M}.filter.PermanentIsLandPredicate", f"{M}.filter.PlayerPredicateTargetFilter",
                        f"{M}.filter.PlayerRelation", f"{M}.filter.PlayerRelationPredicate",
                        f"{M}.filter.TargetFilters", "java.util.List"],
        """        target(TargetFilters.creature())
                // Enchanted creature has "At the beginning of your upkeep, target opponent gains control of this
                // creature unless you sacrifice a land."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        new ForcedCostOrElseEffect(
                                new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                                List.of(new TargetPlayerGainsControlOfSourceCreatureEffect()),
                                true))
                        .withTargetFilter(new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")),
                        GrantScope.ENCHANTED_CREATURE));"""),
    ("n", "NettlevineBlight"): (
        BASE + GRANT + [f"{M}.effect.SacrificeEnchantedPermanentAndReattachSourceAuraEffect",
                        f"{M}.filter.PermanentAnyOfPredicate", f"{M}.filter.PermanentIsCreaturePredicate",
                        f"{M}.filter.PermanentIsLandPredicate", f"{M}.filter.PermanentPredicateTargetFilter",
                        "java.util.List"],
        """        // Enchant creature or land
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()
                )),
                "Target must be a creature or land"
        ))
                // Enchanted permanent has "At the beginning of your end step, sacrifice this permanent
                // and attach Nettlevine Blight to a creature or land you control."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.endStep(ActivePlayer.YOU),
                        new SacrificeEnchantedPermanentAndReattachSourceAuraEffect()),
                        GrantScope.ENCHANTED_PERMANENT));"""),
    ("a", "AdmiralBeckettBrass"): (
        BASE + [f"{M}.CardSubtype", f"{M}.effect.ControlDuration", f"{M}.effect.GainControlOfTargetEffect",
                f"{M}.effect.GrantScope", f"{M}.effect.StaticBoostEffect", f"{M}.filter.PermanentAllOfPredicate",
                f"{M}.filter.PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate",
                f"{M}.filter.PermanentHasAnySubtypePredicate", f"{M}.filter.PermanentIsLandPredicate",
                f"{M}.filter.PermanentNotPredicate", f"{M}.filter.PermanentPredicateTargetFilter",
                f"{M}.trigger.ActivePlayer", f"{M}.trigger.Triggers", "java.util.List", "java.util.Set"],
        """        // Other Pirates you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PIRATE))));

        // At the beginning of your end step, gain control of target nonland permanent
        // controlled by a player who was dealt combat damage by three or more Pirates this turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate(CardSubtype.PIRATE, 3))),
                "Target must be a nonland permanent controlled by a player dealt combat damage by three or more "
                        + "Pirates this turn"))
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT));"""),
}


def write(letter, name, imports, body):
    path = CARDS / letter / f"src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    registrations = re.findall(r"^@CardRegistration\(.*\)$", text, re.M)
    if not registrations:
        raise SystemExit(f"{path}: no @CardRegistration")
    java = sorted(i for i in set(imports) if not i.startswith("java."))
    jdk = sorted(i for i in set(imports) if i.startswith("java."))
    out = [f"package com.github.laxika.magicalvibes.cards.{letter};", ""]
    out += [f"import {i};" for i in java]
    if jdk:
        out += [""] + [f"import {i};" for i in jdk]
    out += [""] + registrations
    out += [f"public class {name} extends Card {{", "", f"    public {name}() {{", body, "    }", "}", ""]
    new = "\n".join(out)
    if new != text:
        path.write_text(new)
        print(f"wrote {letter}/{name}")


for (letter, name), (imports, body) in CARDS_TO_WRITE.items():
    write(letter, name, imports, body)
