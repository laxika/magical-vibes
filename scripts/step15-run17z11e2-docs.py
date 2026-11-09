#!/usr/bin/env python3
"""Step 15, run 17z11e2: the agent-docs rows of the forms the run changed or removed.

- `AllPermanentsUpkeepSacrificeUnlessPayEffect` is gone: "all [permanents] have 'At the beginning of your upkeep,
  sacrifice / destroy this [permanent] unless you pay …'" is a `GrantTriggeredAbilityEffect` of a
  `Triggers.upkeep(YOU)` `ForcedCostOrElseEffect`.
- `DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect` is renamed
  `DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect`, the effect of Shifting Shadow's granted ability.
- `SacrificeEnchantedCreatureEffect` lost `grantedToCreature` (Necrotic Plague grants `SacrificeSelfEffect`).
- Nettlevine Blight grants its end-step ability; `SacrificeEnchantedPermanentAndReattachSourceAuraEffect` reads the
  granting permanent.
- `GainControlIfSubtypesDealtCombatDamageEffect` is gone: Admiral Beckett Brass's target is restricted by the new
  `PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate`.

Idempotent; fails if a doc or skill still names a removed form outside a "gone" line.
Usage: step15-run17z11e2-docs.py [repository root]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

GRANT_FLUX = ("`GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), "
              "new ForcedCostOrElseEffect(cost, List.of(penalty), true)), scope[, filter])`: cost `PayManaCost(\"{N}\")` "
              "or `PayLifeCost(N)`, penalty `SacrificeSelfEffect()` or `DestroyReferencedPermanentEffect(SOURCE)`; scope "
              "`ALL_CREATURES_INCLUDING_SELF` (\"all creatures\"), `ALL_PERMANENTS` + filter (\"other enchantments\"), or "
              "`ALL_PERMANENTS` and `SELF` with the same filter (\"all artifacts\", the source included while it is one)")

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""| `ForcedCostOrElseEffect(SacrificePermanentCost(PermanentIsLandPredicate(), \\"Sacrifice a land\\"), List.of(TargetPlayerGainsControlOfEnchantedPermanentEffect()), true)` | `Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` | Custody Battle (`ONS`). The enchanted creature's controller controls the ability and chooses the target opponent; the normal forced-cost payer is intentional, so the opponent is only the control recipient. |""",
         """| `GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), ForcedCostOrElseEffect(SacrificePermanentCost(PermanentIsLandPredicate(), \\"Sacrifice a land\\"), List.of(TargetPlayerGainsControlOfSourceCreatureEffect()), true)).withTargetFilter(PlayerPredicateTargetFilter(OPPONENT)), ENCHANTED_CREATURE)` | STATIC | Custody Battle (`ONS`). The granted ability is the creature's own: its controller controls it, pays and chooses the target opponent with the ability's own target filter; the opponent is only the control recipient. |"""),
        ("""| "All artifacts/creatures/enchantments have 'At the beginning of your upkeep, sacrifice/destroy this permanent unless you pay [cost].'" | `AllPermanentsUpkeepSacrificeUnlessPayEffect(filter, "{N}"[, lifeAmount[, excludeSource]], sacrifice)`; use `.destroy(filter, "{N}")` for the destroy variant | STATIC | Energy Flux (`PermanentIsArtifactPredicate`, `{2}`), Pendrell Mists (`PermanentIsCreaturePredicate`, `{1}`), Vile Consumption (`PermanentIsCreaturePredicate`, 1 life), Aura Flux (`PermanentIsEnchantmentPredicate`, `{2}`, `excludeSource=true`), The Tabernacle at Pendrell Vale (`PermanentIsCreaturePredicate`, `{1}`, destroy). Global grant (any controller's); read by `StepTriggerService` during each upkeep — pushes one `ForcedCostOrElseEffect(PayManaCost, [SacrificeSelfEffect] or [DestroyReferencedPermanentEffect], true) per matching permanent the active player controls, sourced at that permanent. `excludeSource=true` models Aura Flux's "other enchantments" wording. Not part of the layer-system board (no `LayerClassifier` entry) |""",
         f"""| "All artifacts/creatures/enchantments have 'At the beginning of your upkeep, sacrifice/destroy this permanent unless you pay [cost].'" | {GRANT_FLUX} | STATIC | Energy Flux and Kataki (artifacts, `ALL_PERMANENTS` + `SELF`), Pendrell Mists, Magus of the Tabernacle, Vile Consumption (`PayLifeCost(1)`) and The Tabernacle at Pendrell Vale (destroy) (creatures), Aura Flux ("other enchantments"). The ability is each permanent's own, controlled by its controller (the your-upkeep reader reads the grant); a later "loses all abilities" removes it. `AllPermanentsUpkeepSacrificeUnlessPayEffect` is gone |"""),
        ("""| "Enchanted permanent has 'At the beginning of your end step, sacrifice this permanent and attach [this Aura] to a creature or land you control.'" | `SacrificeEnchantedPermanentAndReattachSourceAuraEffect()` | Triggers.endStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER) | Nettlevine Blight (Aura, enchant creature or land). Fires on the enchanted permanent's controller's end step; that player sacrifices it, then moves the Aura (keeping its controller) onto one of their other creatures/lands. No legal destination → Aura stays unattached and dies as an SBA. Handler prompts only when 2+ destinations exist |""",
         """| "Enchanted permanent has 'At the beginning of your end step, sacrifice this permanent and attach [this Aura] to a creature or land you control.'" | `GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU), new SacrificeEnchantedPermanentAndReattachSourceAuraEffect()), GrantScope.ENCHANTED_PERMANENT)` | STATIC | Nettlevine Blight (Aura, enchant creature or land). The granted ability is the permanent's own (its controller's end step); the entry names the Aura as its granting permanent, which the effect moves (keeping its controller) onto another of that player's creatures/lands after the sacrifice. No legal destination → Aura stays unattached and dies as an SBA. Handler prompts only when 2+ destinations exist |"""),
        ("""| `GrantKeywordEffect(HASTE, ENCHANTED_CREATURE)` + `DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect()` | `STATIC` + `Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` | Shifting Shadow (NEC 109); the handler searches the enchanted creature controller's library and reattaches to the exact creature permanent that entered |""",
         """| `GrantKeywordEffect(HASTE, ENCHANTED_CREATURE)` + `GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), new DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect()), ENCHANTED_CREATURE)` | `STATIC` | Shifting Shadow (NEC 109); the granted ability is the creature's own: it destroys its source, reveals its controller's library and attaches the granting Aura (the entry's granting permanent) to the exact creature permanent that entered |"""),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("""| Enchanted creature's controller is the active player | Necrotic Plague, Soul Bleed, Numbing Dose, Erosion (enchanted land) |""",
         """| Enchanted creature's controller is the active player | Soul Bleed, Numbing Dose, Erosion (enchanted land); an ability the Aura grants in quotes (Necrotic Plague's "enchanted creature has '…'") is a `GrantTriggeredAbilityEffect` of a `Triggers.upkeep(ActivePlayer.YOU)` ability instead |"""),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("""- `TargetPlayerGainsControlOfEnchantedPermanentEffect()` — targeted `ControlStealingEffect`: the target player gains permanent control of the permanent attached to the source Aura. Used by Custody Battle as the fallback of its land-sacrifice upkeep trigger.""",
         """- `TargetPlayerGainsControlOfEnchantedPermanentEffect()` — targeted `ControlStealingEffect`: the target player gains permanent control of the permanent attached to the source Aura or Equipment. (Custody Battle's granted "target opponent gains control of this creature" is `TargetPlayerGainsControlOfSourceCreatureEffect`.)"""),
        ("""- `TargetPlayerGainsControlOfEnchantedPermanentEffect()` — targeted: the target player gains permanent control of the source Aura's attached permanent; re-derives the enchanted permanent from `attachedTo` at resolution (Custody Battle)""",
         """- `TargetPlayerGainsControlOfEnchantedPermanentEffect()` — targeted: the target player gains permanent control of the source Aura's attached permanent; re-derives the enchanted permanent from `attachedTo` at resolution"""),
        ("""- `DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect()` — destroys the enchanted creature during its controller's upkeep, reveals that player's library until a creature enters the battlefield, then attaches the source Aura to the exact creature that entered; the other revealed cards go to the bottom randomly (Shifting Shadow)""",
         """- `DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect()` — the effect of the upkeep ability an Aura grants (`GrantTriggeredAbilityEffect`, Shifting Shadow): destroys its source creature, reveals its controller's library until a creature enters the battlefield, then attaches the granting Aura (the entry's granting permanent) to the exact creature that entered; the other revealed cards go to the bottom randomly. A creature or Aura that has left is neither destroyed nor attached"""),
        ("""- `SacrificeEnchantedPermanentAndReattachSourceAuraEffect()` — Nettlevine Blight: `Triggers.endStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` Aura effect; the enchanted permanent's controller sacrifices it and moves this Aura (keeping its controller) onto another creature/land they control""",
         """- `SacrificeEnchantedPermanentAndReattachSourceAuraEffect()` — Nettlevine Blight's granted `Triggers.endStep(ActivePlayer.YOU)` ability (`GrantTriggeredAbilityEffect`, `ENCHANTED_PERMANENT`): its controller sacrifices the permanent and moves the granting Aura (keeping its controller) onto another creature/land they control; in an Aura's own ability (Breath of Fury) the Aura is the source"""),
        ("""- `AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, String manaCost[, boolean excludeSource])` — STATIC global marker: grants every matching permanent "At the beginning of your upkeep, sacrifice this permanent unless you pay {N}." Read by `StepTriggerService` (not the layer system) — one `ForcedCostOrElseEffect` pay-or-sacrifice trigger per matching permanent the active player controls, sourced at that permanent. Grant is global regardless of who controls the enchantment (Energy Flux artifacts `{2}`, Pendrell Mists creatures `{1}`, Aura Flux other enchantments `{2}`); `excludeSource` excludes the permanent carrying the grant
- `AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, String manaCost, int lifeAmount)` — STATIC global marker: grants every matching permanent "At the beginning of your upkeep, sacrifice this permanent unless you pay [cost]." Read by `StepTriggerService` (not the layer system) — one `ForcedCostOrElseEffect` pay-or-sacrifice trigger per matching permanent the active player controls, sourced at that permanent. Grant is global regardless of who controls the enchantment (Energy Flux artifacts `{2}`, Pendrell Mists creatures `{1}`, Vile Consumption creatures 1 life)
- `AllPermanentsUpkeepSacrificeUnlessPayEffect.destroy(PermanentPredicate filter, String manaCost)` — STATIC global marker: grants every matching permanent "At the beginning of your upkeep, destroy this permanent unless you pay {N}." It uses the same global trigger collection as the sacrifice variant but resolves the unpaid penalty through `DestroyReferencedPermanentEffect(SOURCE)`, so regeneration and indestructible are respected (The Tabernacle at Pendrell Vale)""",
         f"""- "All [permanents] have 'At the beginning of your upkeep, sacrifice / destroy this permanent unless you pay [cost].'" — {GRANT_FLUX} (Energy Flux, Kataki, Pendrell Mists, Magus of the Tabernacle, Vile Consumption, The Tabernacle at Pendrell Vale, Aura Flux). `AllPermanentsUpkeepSacrificeUnlessPayEffect` is gone"""),
    ],
    "EFFECTS_INDEX.md": [
        ("""re-derives the attached permanent from `attachedTo` at resolution (Custody Battle, Assault Suit) |""",
         """re-derives the attached permanent from `attachedTo` at resolution (Assault Suit) |"""),
        ("""| `SacrificeEnchantedCreatureEffect` | `()` | sacrifice the enchanted creature without additional effects. Use with `Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` for "at the beginning of your upkeep, sacrifice this creature" (e.g. Necrotic Plague) |""",
         """| `SacrificeEnchantedCreatureEffect` | `()` | the Aura's own ability sacrifices the creature it enchants (Lingering Death, Thirsting Axe). "Enchanted creature has 'At the beginning of your upkeep, sacrifice this creature'" (Necrotic Plague) is a granted ability with `SacrificeSelfEffect` (`GrantTriggeredAbilityEffect`, `ENCHANTED_CREATURE`); `grantedToCreature` is gone |"""),
        ("""| `DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect` | `()` | destroys the source Aura's enchanted permanent, reveals cards from that permanent's controller's library until a creature is found, puts it onto the battlefield, and attaches the source Aura to that exact permanent; other revealed cards are put on the bottom randomly. Used by Shifting Shadow (NEC 109) |""",
         """| `DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect` | `()` | the effect of the upkeep ability an Aura grants (`GrantTriggeredAbilityEffect`, `ENCHANTED_CREATURE`): destroys its source creature, reveals cards from its controller's library until a creature is found, puts it onto the battlefield, and attaches the granting Aura (`StackEntry.requireGrantingPermanentId()`) to that exact permanent; other revealed cards are put on the bottom randomly. Used by Shifting Shadow (NEC 109) |"""),
        ("""| `GainControlIfSubtypesDealtCombatDamageEffect` | `(CardSubtype subtype, int threshold)` | end-step trigger: gain control of target nonland permanent controlled by a player dealt combat damage by `threshold`+ creatures of `subtype` this turn. Intervening-if + targeting handled by StepTriggerService; resolves via `GainControlOfTargetEffect(PERMANENT)` |""",
         """| `GainControlIfSubtypesDealtCombatDamageEffect` | gone | "gain control of target nonland permanent controlled by a player who was dealt combat damage by three or more Pirates this turn" (Admiral Beckett Brass) is `GainControlOfTargetEffect(PERMANENT)` with the target filter `PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate(PIRATE, 3)` |"""),
        ("""| `SacrificeEnchantedPermanentAndReattachSourceAuraEffect` | `()` | Nettlevine Blight. `Triggers.endStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` Aura effect (fires on the enchanted permanent's controller's end step; slot handled in `StepTriggerService.handleEndStepTriggers`, keyed on `findPermanentController(aura.attachedTo) == activePlayer`). Resolution: that player sacrifices the enchanted permanent, then moves the Aura (via `Permanent.setAttachedTo`, keeping its controller) onto another creature/land they control. Uses `entry.getSourcePermanentId()` for the Aura.""",
         """| `SacrificeEnchantedPermanentAndReattachSourceAuraEffect` | `()` | Nettlevine Blight's granted `Triggers.endStep(ActivePlayer.YOU)` ability (`GrantTriggeredAbilityEffect`, `ENCHANTED_PERMANENT`; the your-end-step reader records the Aura as the entry's granting permanent). Resolution: the ability's controller sacrifices its source permanent, then moves the granting Aura (via `Permanent.setAttachedTo`, keeping its controller) onto another creature/land they control. In an Aura's own ability (Breath of Fury) the Aura is the source."""),
        ("""| `AllPermanentsUpkeepSacrificeUnlessPayEffect` | `(PermanentPredicate filter, String manaCost[, int lifeAmount, boolean excludeSource, boolean sacrifice])`; `.destroy(filter, manaCost)` factory | STATIC global marker (Energy Flux artifacts/{2}, Pendrell Mists creatures/{1}, Aura Flux other enchantments/{2}, The Tabernacle at Pendrell Vale creatures/{1} with `sacrifice=false`): grants every matching permanent "At the beginning of your upkeep, sacrifice/destroy this permanent unless you pay {N}." Read by `StepTriggerService.handleGrantedArtifactSacrificeTriggers` (has no `LayerClassifier` entry, so the board ignores it): during each upkeep, for every matching permanent the active player controls it pushes a `ForcedCostOrElseEffect(PayManaCost(manaCost), [SacrificeSelfEffect()] or [DestroyReferencedPermanentEffect(SOURCE)], true)` trigger sourced at that permanent (so the pay prompt and the penalty both act on the individual permanent). Global - fires regardless of who controls the enchantment; multiple grants each push their own trigger. When `excludeSource` is true, the permanent carrying the grant is skipped. |""",
         f"""| `AllPermanentsUpkeepSacrificeUnlessPayEffect` | gone | "all [permanents] have 'At the beginning of your upkeep, sacrifice / destroy this permanent unless you pay [cost]'" is {GRANT_FLUX} (Energy Flux, Kataki, Pendrell Mists, Magus of the Tabernacle, Vile Consumption, The Tabernacle at Pendrell Vale, Aura Flux) |"""),
    ],
    "PREDICATES_REFERENCE.md": [
        ("""| `PermanentControllerGraveyardCountAtLeastPredicate` | `(int minimumGraveyardCards)` | permanents whose current controller has at least N cards in their graveyard |""",
         """| `PermanentControllerGraveyardCountAtLeastPredicate` | `(int minimumGraveyardCards)` | permanents whose current controller has at least N cards in their graveyard |
| `PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate` | `(CardSubtype subtype, int minimumSources)` | permanents whose current controller was dealt combat damage this turn by at least N different creatures of that subtype (a changeling counts; a creature counts once, even if it has left or lost the type): Admiral Beckett Brass's "target nonland permanent controlled by a player who was dealt combat damage by three or more Pirates this turn" as the card's target filter |"""),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("""| Aura upkeep destruction, creature reveal, random-bottom, and automatic reattachment to the revealed creature | `s/ShiftingShadow.java` + `DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect` |""",
         """| Aura-granted upkeep destruction, creature reveal, random-bottom, and automatic reattachment to the revealed creature | `s/ShiftingShadow.java` + `GrantTriggeredAbilityEffect` of `DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect` |"""),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("""| Grant all artifacts/creatures/enchantments an upkeep pay-or-sacrifice/destroy ability | `e/EnergyFlux.java`, `p/PendrellMists.java`, `a/AuraFlux.java`, `t/TheTabernacleAtPendrellVale.java` | STATIC AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentIsArtifactPredicate, "{2}") / (PermanentIsCreaturePredicate, "{1}") / (PermanentIsEnchantmentPredicate, "{2}", excludeSource=true) / `.destroy(PermanentIsCreaturePredicate, "{1}")` — global enchantment/land; StepTriggerService pushes one ForcedCostOrElseEffect(PayManaCost, [SacrificeSelfEffect] or [DestroyReferencedPermanentEffect(SOURCE)], true) per matching permanent the active player controls (sourced at that permanent) during their upkeep. Fires regardless of who controls the grant; `excludeSource=true` models Aura Flux's "other enchantments" wording |""",
         f"""| Grant all artifacts/creatures/enchantments an upkeep pay-or-sacrifice/destroy ability | `e/EnergyFlux.java`, `p/PendrellMists.java`, `a/AuraFlux.java`, `t/TheTabernacleAtPendrellVale.java` | STATIC {GRANT_FLUX}. The granted ability is each permanent's own, on its controller's upkeep, whoever controls the grant |"""),
    ],
}

# Every ForcedCostOrElseEffect row: a "that player" payment's paid effects target nothing as the ability triggers.
PAYER_OLD = "instead of the source controller — works for"
PAYER_NEW = ("instead of the source controller; its paid effects act for that player after they pay, so the ability "
             "targets nothing for them as it triggers (Fraying Line) — works for")
quick = DOCS / "EFFECTS_QUICK_REFERENCE.md"
quick.write_text(quick.read_text().replace(PAYER_OLD, PAYER_NEW))

REMOVED = ["AllPermanentsUpkeepSacrificeUnlessPayEffect", "DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect",
           "grantedToCreature", "GainControlIfSubtypesDealtCombatDamageEffect"]

for name, edits in EDITS.items():
    path = DOCS / name
    text = path.read_text()
    for old, new in edits:
        if new in text:
            continue
        if text.count(old) != 1:
            raise SystemExit(f"{name}: expected one match of {old[:90]!r}, found {text.count(old)}")
        text = text.replace(old, new)
    path.write_text(text)

problems = []
for path in list(DOCS.rglob("*.md")) + list((ROOT / ".claude").rglob("*.md")):
    if path.name == "EFFECT_MERGE_PLAN.md":
        continue
    for number, line in enumerate(path.read_text().splitlines(), 1):
        for removed in REMOVED:
            if removed in line and "gone" not in line:
                problems.append(f"{path.relative_to(ROOT)}:{number}: {removed}")
if problems:
    raise SystemExit("still named:\n" + "\n".join(problems))
print("docs updated")
