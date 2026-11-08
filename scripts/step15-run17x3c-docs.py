#!/usr/bin/env python3
"""Step 15, run 17x3c: agent-docs name the enter family's "it" as the stack entry's triggering permanent.

Rewrites the doc lines that described an effect bound to the entering permanent by the enter collector (the removed
`CardEffect.usesEnteringPermanentReference()` and the class-keyed collector handlers), the renamed
`DealDamageToTriggeringPermanentEffect` / `DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect`, the
connive form `ofTriggeringPermanent()`, graft's and soulbond's card-written intervening "if" and "you may", and adds
`PermanentIsPairedPredicate` and `TriggeringPermanentPower(true)`. Each edit replaces the one line that starts with
(or contains) its key; it fails if a key matches no line or several. Idempotent: a line already rewritten is left
alone. It then fails if a doc or skill still names a removed form.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

TP = "the stack entry's triggering permanent, read as the ability resolves"

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateTokenCopyOfEnteringPermanentEffect([boolean grantHaste",
         "- `CreateTokenCopyOfEnteringPermanentEffect([boolean grantHaste, boolean exileAtEndStep[, boolean sacrificeAtEndStep]])` - non-targeting copy of \"it\": " + TP + " (its copiable values as it last existed on the battlefield if it has left). Optional flags reuse the shared token-copy profile for haste, next-end-step exile, or sacrifice; the no-arg form remains a plain copy. The five-argument form additionally supports tracking created tokens with the source and creating them under the entering permanent controller (Dual Nature)."),
        ("- `EnteringCreatureSourcePowerBranchEffect(",
         "- `EnteringCreatureSourcePowerBranchEffect(CardEffect sourcePowerAtLeast, CardEffect belowSourcePower)` - compares the power of the stack entry's triggering permanent with the source permanent's as the ability resolves (each one's last-known power once it has left, CR 608.2h); a branch about \"that creature\" is written on the triggering permanent (`PutCounterOnTargetPermanentEffect.onTriggeringPermanent`). Shelinda, Yevon Acolyte (FIC 94)"),
        ("- Palazzo Archers uses `Triggers.creatureAttacksYouOrYourPlaneswalker()",
         "- Palazzo Archers uses `Triggers.creatureAttacksYouOrYourPlaneswalker().withFilter(new PermanentHasKeywordPredicate(FLYING))` with `DealDamageToTriggeringPermanentEffect(new SourcePower())`; the damage effect evaluates its `DynamicAmount` from the source permanent when the trigger resolves."),
        ("- `SetSelfBasePowerToughnessFromEnteringCreatureEffect()`",
         "- `SetSelfBasePowerToughnessFromEnteringCreatureEffect()` — sets the source's base power and toughness until end of turn to those of \"that creature\": " + TP + " (as it last existed if it has left). Eldrazi Mimic (`OGW`)"),
        ("- `CreateTokenCopyOfEnteringPermanentEffect()` — non-targeting copy effect",
         "- `CreateTokenCopyOfEnteringPermanentEffect()` — non-targeting copy of the stack entry's triggering permanent (the permanent that entered). Used by Ondu Spiritdancer."),
        ("- `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect()` — `Triggers.enters(",
         "- `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect()` — on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)`: as it resolves, reads the power of " + TP.split(", read")[0] + " (live, else as it last existed), offers the controller an optional payment of that much life, and puts that many +1/+1 counters on it if paid (Madame Null, Power Broker)."),
        ("- `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect()` — Triggers.enters(",
         "- `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect()` — see above: the power of the stack entry's triggering permanent as the ability resolves, paid as life for that many +1/+1 counters on it (Madame Null, Power Broker)"),
        ("- `EndureEffect(int amount)` / `EndureEffect(DynamicAmount)`",
         "- `EndureEffect(int amount)` / `EndureEffect(DynamicAmount)` — resolution-time keyword action: choose between putting `amount` +1/+1 counters on the instructed permanent and creating one `amount`/`amount` white Spirit creature token. The default target is the source; `EndureEffect.forTriggeringPermanent(DynamicAmount)` instructs \"it\", the stack entry's triggering permanent (the creature that entered or was turned face up). If the instructed permanent has left the battlefield or cannot have +1/+1 counters, the token branch is automatic. `new XValue()` uses an ability's announced X. Used by Anafenza, Unyielding Lineage, Krumar Initiate, and Warden of the Grove."),
        ("- `DealDamageToTriggeringAttackerEffect(DynamicAmount damage)`",
         "- `DealDamageToTriggeringPermanentEffect(DynamicAmount damage)`; convenience `(int)` — \"this deals N damage to it\": evaluated damage to the stack entry's triggering permanent (referenced, not targeted; protection from the source prevents it). The trigger's `withFilter(...)` restricts which permanents trigger it: `Triggers.creatureAttacksYouOrYourPlaneswalker()` / `Triggers.creatureAttacksYou()` (Raking Canopy: flyers, 4; Barbed Foliage: direct nonflyers, 1; Storm, Queen of Wakanda: flyers, `SourcePower()`), `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` (Aether Flash, 2)"),
        ("- `DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect(",
         "- `DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect(int damage, PermanentPredicate boostTargetPredicate, int powerBoost, int toughnessBoost)` — enter trigger: deals damage to \"it\", the stack entry's triggering permanent (not targeted), and boosts the source only if a permanent matching the predicate was dealt damage this way (Marauding Raptor)"),
        ("- `ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect()`",
         "- `ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect()` — graveyard creature-enter trigger: return the source Aura or Equipment from its owner's graveyard, then attach it to \"that creature\", " + TP + ", when possible (Dragon Scales, Sword of the Meek)"),
        ("- `DrawDiscardAndConniveEffect([DynamicAmount amount[, boolean targetPermanent]])`",
         "- `DrawDiscardAndConniveEffect([DynamicAmount amount[, boolean targetPermanent]])` — draw and discard the evaluated amount, then put a +1/+1 counter on the source or targeted creature for each nonland card discarded. The no-argument and boolean forms default to one card; a pending hook applies counters after each interactive discard. Used by Doc Ock's Henchmen, Unstable Experiment, and Raffine, Scheming Seer. \"It connives\" of a trigger about a permanent is `DrawDiscardAndConniveEffect.ofTriggeringPermanent()`: the stack entry's triggering permanent connives (Baron Strucker, Swordsman)."),
        ("- `MoveCounterFromSourceToEnteringCreatureEffect(CounterType[, UUID])`",
         "- `MoveCounterFromSourceToEnteringCreatureEffect(CounterType)` — non-targeting \"move one counter from this permanent onto that creature\": the creature is " + TP + "; placement goes through `placeCounterOnPermanent`. Graft writes it in a `MayEffect` behind the intervening \"if\" `SourceCounterThreshold(1, PLUS_ONE_PLUS_ONE)` (CR 702.58a), asked as it resolves (CR 603.5)"),
        ("- `MoveChosenCounterFromSourceToEnteringCreatureEffect(List<CounterType>)`",
         "- `MoveChosenCounterFromSourceToEnteringCreatureEffect(List<CounterType>)` — non-targeting: moves one counter of a kind the source controller chooses among the supplied types still on the source onto \"that creature\", " + TP + "; the card writes \"you may\" as a `MayEffect` (Agent's Toolkit, NCC 66)"),
        ("- `MoveCounterFromSourceToEnteringCreatureEffect(CounterType)` — non-targeting `Triggers.enters(",
         "- Graft: `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` with `TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE))` and `new MayEffect(new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE), \"Move a +1/+1 counter onto that creature?\")` (Simic Initiate)"),
        ("- `SoulbondPairWithEnteringEffect()`",
         "- `SoulbondPairWithEnteringEffect()` — soulbond's \"another creature you control enters\" half (CR 702.95a): pairs this creature with \"that creature\", " + TP + ". The card writes it as `new MayEffect(new SoulbondPairWithEnteringEffect(), \"Pair X with that creature?\")` on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` behind the intervening \"if\" `new AllConditions(List.of(new NotCondition(new SourceIsPaired()), new TriggeringPermanentMatches(new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(), new PermanentNotPredicate(new PermanentIsPairedPredicate()))))))` (Druid's Familiar)"),
        ("- `BecomeSaddledUntilEndOfTurnEffect(GrantScope)`",
         "- `BecomeSaddledUntilEndOfTurnEffect(GrantScope)` — marks a Mount as saddled until cleanup; `SELF` and `TARGET` scopes use the normal source/target permanent resolution paths, `TRIGGERING_PERMANENT` the stack entry's triggering permanent (Kolodin's \"it becomes saddled\"), and `Permanent.resetModifiers()` clears the temporary designation"),
        ("- `PutCountersOnEnteringCreatureEqualToSourceCountersEffect()`",
         "- `PutCountersOnEnteringCreatureEqualToSourceCountersEffect()` - `Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_CONTROL)`: copies the source permanent's current count of every concrete counter kind onto \"that creature\", the stack entry's triggering permanent. Denry Klin's intervening-if gate is `TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.ANY))` on the trigger; the handler uses the shared counter-placement pipeline for each kind."),
        ("`TargetPower(true)` preserves negative power",
         "`TargetPower(true)` preserves negative power when a power boost doubles a creature's power (Bulk Up); the default `TargetPower()` continues to clamp negative amounts to zero. `TriggeringPermanentPower(true)` likewise keeps a negative power of the triggering permanent (Belligerent Yearling's \"base power becomes equal to that creature's power\")."),
    ],
    "EFFECTS_INDEX.md": [
        ("- `CreateTokenCopyOfEnteringPermanentEffect` - `()`",
         "- `CreateTokenCopyOfEnteringPermanentEffect` - `()` or `(boolean grantHaste, boolean exileAtEndStep)`; non-targeting copy of the stack entry's triggering permanent (the permanent that entered), as it is when the ability resolves or as it last existed. Optional flags support haste and next-end-step exile (Molten Echoes)."),
        ("| `EnteringCreatureSourcePowerBranchEffect` |",
         "| `EnteringCreatureSourcePowerBranchEffect` | `(CardEffect sourcePowerAtLeast, CardEffect belowSourcePower)` | Compares the power of the stack entry's triggering permanent with the source's as the ability resolves (last-known powers once either has left); dispatches the branch, whose \"that creature\" is written on the triggering permanent (`PutCounterOnTargetPermanentEffect.onTriggeringPermanent`). | Shelinda, Yevon Acolyte (FIC 94) |"),
        ("| `DealDamageToTriggeringAttackerEffect` dynamic amount |",
         "| `DealDamageToTriggeringPermanentEffect` dynamic amount | `(DynamicAmount damage)` | evaluates source-relative amounts such as `SourcePower`; the trigger's filter on the attacker is checked when attackers are declared | Palazzo Archers (ACR 42) |"),
        ("| `SetSelfBasePowerToughnessFromEnteringCreatureEffect` |",
         "| `SetSelfBasePowerToughnessFromEnteringCreatureEffect` | `()` | sets the source's base power and toughness until end of turn to those of the stack entry's triggering permanent as the ability resolves (as it last existed if it has left). Eldrazi Mimic (`OGW`) |"),
        ("- `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect` |",
         "- `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect` | `()` | on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)`: as it resolves, reads the power of the stack entry's triggering permanent (live, else as it last existed) and offers `MayPayLifeEffect(power, PutCounterOnTargetPermanentEffect.onTriggeringPermanent(PLUS_ONE_PLUS_ONE, power))`. Used by Madame Null, Power Broker."),
        ("| `EndureEffect` |",
         "| `EndureEffect` | `(int amount)` / `(DynamicAmount amount)` | resolution-time keyword action: the controller chooses between putting `amount` +1/+1 counters on the instructed permanent and creating one `amount`/`amount` white Spirit creature token. The default target is the source; `EndureEffect.forTriggeringPermanent(DynamicAmount)` instructs the stack entry's triggering permanent (\"it endures\"). If the instructed permanent has left the battlefield or cannot have +1/+1 counters, token creation is automatic. `new XValue()` uses an ability's announced X. Used by Anafenza, Unyielding Lineage, Krumar Initiate, and Warden of the Grove. |"),
        ("| `DealDamageToTriggeringAttackerEffect` |",
         "| `DealDamageToTriggeringPermanentEffect` | `(DynamicAmount damage)`; convenience `(int)` | \"this deals N damage to it\": the evaluated damage to the stack entry's triggering permanent (referenced, not targeted; protection from the source prevents it, CR 702.16e). The trigger's `withFilter(...)` restricts which permanents fire it. Used by Raking Canopy (`creatureAttacksYou()` with `PermanentHasKeywordPredicate(FLYING)`, 4 damage), Storm, Queen of Wakanda (`SourcePower()`) and Aether Flash (`enters(CREATURE, ANOTHER)`, 2) |"),
        ("| `DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect` |",
         "| `DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect` | `(int damage, PermanentPredicate boostTargetPredicate, int powerBoost, int toughnessBoost)` | deals damage to the stack entry's triggering permanent (not targeted), then boosts the source when a permanent matching the predicate was dealt damage this way (Marauding Raptor) |"),
        ("| `ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect` |",
         "| `ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect` | `()` | graveyard creature-enter trigger: return the source Aura or Equipment from its owner's graveyard and attach it to the stack entry's triggering permanent when possible (Dragon Scales, Sword of the Meek) |"),
        ("marker for graft-style \"you may move a counter from this permanent onto it\"", None),
        ("| `MoveCounterFromSourceToEnteringCreatureEffect` | `(CounterType[, UUID enteringPermanentId])` |",
         "| `MoveCounterFromSourceToEnteringCreatureEffect` | `(CounterType)` | Moves one counter of that type from the source permanent onto the stack entry's triggering permanent as the ability resolves (no-op if either is gone or the source lacks one), through `PermanentCounterSupport`. `targetSpec()` is `NONE`. Graft (CR 702.58a) writes it in a `MayEffect` behind the intervening \"if\" `SourceCounterThreshold(1, PLUS_ONE_PLUS_ONE)` (Simic Initiate, Llanowar Reborn); Gatewatch Beacon's loyalty form the same with `LOYALTY` |"),
        ("| `MoveChosenCounterFromSourceToEnteringCreatureEffect` |",
         "| `MoveChosenCounterFromSourceToEnteringCreatureEffect` | `(List<CounterType>)` | Moves one counter of a kind the source controller chooses among the supplied types still on the source onto the stack entry's triggering permanent; the card writes \"you may\" as a `MayEffect` (Agent's Toolkit). `targetSpec()` is `NONE` |"),
        ("| `BecomeSaddledUntilEndOfTurnEffect` |",
         "| `BecomeSaddledUntilEndOfTurnEffect` | `(GrantScope scope)` | one-shot temporary Mount status: marks the selected permanent as saddled until cleanup; `SELF` uses the source permanent, `TARGET` the chosen permanent and `TRIGGERING_PERMANENT` the stack entry's triggering permanent (Kolodin) |"),
        ("| `DrawDiscardAndConniveEffect` |",
         "| `DrawDiscardAndConniveEffect` | `()` or `(boolean targetPermanent)` or `(DynamicAmount amount[, boolean targetPermanent])`; `ofTriggeringPermanent()` | Draw and discard the evaluated amount; put a +1/+1 counter on the source or targeted creature (with `ofTriggeringPermanent()`, the stack entry's triggering permanent: \"it connives\") for each nonland card discarded. The no-argument and boolean forms default to one card. Counters are applied via a pending hook after each interactive discard. Used by Doc Ock's Henchmen, Unstable Experiment, and Raffine, Scheming Seer |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Lifelink. Whenever another creature you control enters, put a +1/+1 counter on that creature if its power is less than Shelinda's power.",
         "| \"Lifelink. Whenever another creature you control enters, put a +1/+1 counter on that creature if its power is less than Shelinda's power. Otherwise, put a +1/+1 counter on Shelinda.\" | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) EnteringCreatureSourcePowerBranchEffect(PutCountersOnSourceEffect(1, 1, 1), PutCounterOnTargetPermanentEffect.onTriggeringPermanent(PLUS_ONE_PLUS_ONE, 1))` | Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL) | Shelinda, Yevon Acolyte (FIC 94) |"),
        ("| \"Whenever a creature with flying attacks you or a planeswalker you control, this creature deals damage equal to its power to that creature\" |",
         "| \"Whenever a creature with flying attacks you or a planeswalker you control, this creature deals damage equal to its power to that creature\" | `DealDamageToTriggeringPermanentEffect(new SourcePower())` | `Triggers.creatureAttacksYouOrYourPlaneswalker().withFilter(new PermanentHasKeywordPredicate(FLYING))` | The trigger's filter is judged on the attacker at declaration; the source power is evaluated when the trigger resolves. Used by Palazzo Archers (ACR 42) |"),
        ("| \"Whenever another colorless creature you control enters, you may change this creature's base power and toughness",
         "| \"Whenever another colorless creature you control enters, you may change this creature's base power and toughness to that creature's power and toughness until end of turn\" | `MayEffect(SetSelfBasePowerToughnessFromEnteringCreatureEffect())` | Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentIsColorlessPredicate()), ObjectRelation.ANOTHER_YOU_CONTROL) | \"That creature\" is the stack entry's triggering permanent; its power and toughness are read as the ability resolves (as it last existed if it has left). Eldrazi Mimic (`OGW`) |"),
        ("| \"Whenever a creature with flying attacks you, this deals N damage to it\" |",
         "| \"Whenever a creature with flying attacks you, this deals N damage to it\" | `DealDamageToTriggeringPermanentEffect(N)` | `Triggers.creatureAttacksYou().withFilter(new PermanentHasKeywordPredicate(FLYING))` | The trigger's filter restricts which attackers fire it (judged at attack declaration in `CombatAttackService`); damage is dealt to the attacking creature (the stack entry's triggering permanent, not targeted) on resolution. Used by Raking Canopy (4 damage) |"),
        ("| \"Soulbond\" / \"you may pair this creature with another unpaired creature when either enters\" |",
         "| \"Soulbond\" / \"you may pair this creature with another unpaired creature when either enters\" | `MayEffect(SoulbondChoosePartnerEffect)` on `Triggers.enters(ObjectRelation.THIS)` + `MayEffect(SoulbondPairWithEnteringEffect, \"Pair X with that creature?\")` on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` behind the intervening \"if\" `AllConditions(NotCondition(SourceIsPaired), TriggeringPermanentMatches(PermanentAllOfPredicate(PermanentControlledBySourceControllerPredicate, PermanentNotPredicate(PermanentIsPairedPredicate))))` | TRIGGER | CR 702.95a. Pairs stored on `Permanent.pairedWithId`; \"that creature\" is the stack entry's triggering permanent. Deadeye Navigator |"),
        ("| \"Whenever a creature enters, this enchantment deals N damage to it\" |",
         "| \"Whenever a creature enters, this enchantment deals N damage to it\" | `DealDamageToTriggeringPermanentEffect(N)` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` | Aether Flash. \"It\" is the stack entry's triggering permanent (not targeted, source = this permanent). Fires for every creature (any controller). |"),
        ("| \"Whenever another creature you control enters, this creature deals N damage to it. If it is a [subtype]",
         "| \"Whenever another creature you control enters, this creature deals N damage to it. If it is a [subtype], this creature gets +P/+T until end of turn\" | `DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect(N, subtype predicate, P, T)` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | Marauding Raptor. \"It\" is the stack entry's triggering permanent; the boost requires a matching permanent to be dealt damage this way. |"),
        ("| \"Whenever another creature you control enters, you may pay life equal to its power.",
         "| \"Whenever another creature you control enters, you may pay life equal to its power. If you do, put that many +1/+1 counters on it\" | `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect()` | `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | Madame Null, Power Broker. \"It\" is the stack entry's triggering permanent: its power is read as the ability resolves (as it last existed if it has left), so the controller may pay even if the creature is gone, but counters are placed only if it is still present. |"),
        ("| \"Graft N (This permanent enters with N +1/+1 counters on it.",
         "| \"Graft N (This permanent enters with N +1/+1 counters on it. Whenever another creature enters, you may move a +1/+1 counter from this permanent onto that creature.)\" | `EnterWithCountersEffect(PLUS_ONE_PLUS_ONE, new Fixed(N))` + `TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, PLUS_ONE_PLUS_ONE))` with `MayEffect(MoveCounterFromSourceToEnteringCreatureEffect(PLUS_ONE_PLUS_ONE), \"Move a +1/+1 counter onto that creature?\")` | `Triggers.enters(ObjectRelation.THIS)` + `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` | CR 702.58a: \"if this permanent has a +1/+1 counter on it\" is an intervening \"if\" (CR 603.4); the ability goes on the stack and asks as it resolves (CR 603.5); \"that creature\" is the stack entry's triggering permanent. Used by Llanowar Reborn and Cytoplast Manipulator (`DIS`). |"),
        ("| \"This artifact enters with a +1/+1 counter, a flying counter, a deathtouch counter, and a shield counter on it.",
         "| \"This artifact enters with a +1/+1 counter, a flying counter, a deathtouch counter, and a shield counter on it. Whenever a creature you control enters, you may move a counter from this artifact onto that creature.\" | four `EnterWithCountersEffect(type, new Fixed(1))` replacements + `MayEffect(MoveChosenCounterFromSourceToEnteringCreatureEffect(List.of(PLUS_ONE_PLUS_ONE, FLYING, DEATHTOUCH, SHIELD, …)), …)` | `Triggers.enters(ObjectRelation.THIS)` + `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | The ability goes on the stack and asks as it resolves (CR 603.5); \"that creature\" is the stack entry's triggering permanent; the source controller chooses one of the supplied counter kinds still present. Agent's Toolkit (`NCC` 66). |"),
        ("| \"Villain spells you cast cost {1} less to cast. Whenever another Villain you control enters, you may have it connive.",
         "| \"Villain spells you cast cost {1} less to cast. Whenever another Villain you control enters, you may have it connive. Do this only once each turn\" | `ReduceCastCostForMatchingSpellsEffect(CardSubtypePredicate(VILLAIN), 1, SELF)` + `MayEffect(DrawDiscardAndConniveEffect.ofTriggeringPermanent(), \"Have it connive?\")` with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` | STATIC + Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(VILLAIN)), ObjectRelation.ANOTHER_YOU_CONTROL) | Baron Strucker, HYDRA Overlord (MSH 88). \"It\" is the stack entry's triggering permanent; the once-per-turn marker is consumed only when the may choice is accepted. |"),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("| Soulbond + paired flicker grant |",
         "| Soulbond + paired flicker grant | `d/DeadeyeNavigator.java` | SoulbondChoosePartnerEffect + MayEffect(SoulbondPairWithEnteringEffect) behind the both-unpaired intervening \"if\"; STATIC ConditionalEffect(SourceIsPaired, GrantActivatedAbilityEffect(flickerSelfUnderYourControl, SELF_AND_PAIRED)) |"),
        ("| Soulbond + paired triggered ability |",
         "| Soulbond + paired triggered ability | `t/TandemLookout.java` | SoulbondChoosePartnerEffect + MayEffect(SoulbondPairWithEnteringEffect) behind the both-unpaired intervening \"if\"; STATIC ConditionalEffect(SourceIsPaired, GrantTriggeredAbilityEffect(Triggers.dealsDamageToPlayer(), DrawCardEffect(1), SELF_AND_PAIRED)) |"),
        ("| Soulbond + paired haste |",
         "| Soulbond + paired haste | `l/LightningMauler.java` | SoulbondChoosePartnerEffect + MayEffect(SoulbondPairWithEnteringEffect) behind the both-unpaired intervening \"if\"; STATIC ConditionalEffect(SourceIsPaired, GrantKeywordEffect(HASTE, SELF_AND_PAIRED)) |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| creature-entry trigger may move one chosen counter from this artifact onto that creature |",
         "| creature-entry trigger may move one chosen counter from this artifact onto that creature | `a/AgentsToolkit.java` + `MayEffect(MoveChosenCounterFromSourceToEnteringCreatureEffect)` |"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentIsSourceOrPairedPredicate` |",
         "| `PermanentIsSourceOrPairedPredicate` | `()` | the source permanent itself or its soulbond partner | `sourcePermanentId` / `sourcePermanentSnapshot` |\n"
         "| `PermanentIsPairedPredicate` | `()` | the permanent is soulbond-paired with another creature (CR 702.95); soulbond's \"both are unpaired\" reads it on the triggering permanent through `TriggeringPermanentMatches(new PermanentNotPredicate(…))` | — |"),
    ],
}

# Fragment renames inside longer lines (the capability lists).
FRAGMENTS = {
    "EFFECTS_INDEX.md": [("`DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect`",
                          "`DealDamageToTriggeringPermanentThenBoostSourceIfDamagedEffect`")],
}

# A sentence appended once to ARCHITECTURE.md's triggering-permanent paragraph.
ARCH_ANCHOR = "the enter collector puts them on the stack as written."
ARCH_SENTENCE = (" No effect asks the enter collector to bind the entering permanent as its target or source any more:"
                 " graft, Agent's Toolkit, soulbond's other half, \"it connives\", \"deals N damage to it\""
                 " (`DealDamageToTriggeringPermanentEffect`), endure, the power branches, Madame Null, Mana Echoes,"
                 " Eldrazi Mimic and the graveyard Auras and Equipment that return attached to \"that creature\" read"
                 " the entry's triggering permanent as they resolve, and a \"you may\" goes on the stack and asks as it"
                 " resolves (CR 603.5).")

REMOVED = ["usesEnteringPermanentReference", "DealDamageToTriggeringAttackerEffect",
           "DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect", "DrawDiscardAndConniveEffect.forEnteringPermanent"]


def edit_doc(name, edits):
    path = DOCS / name
    lines = path.read_text().split("\n")
    for key, new in edits:
        if new is not None and all(part in lines for part in new.split("\n")):
            continue
        matches = [i for i, line in enumerate(lines) if line.startswith(key)] \
            or [i for i, line in enumerate(lines) if key in line]
        if new is None:
            if not matches:
                continue
            if len(matches) != 1:
                raise SystemExit(f"{name}: {len(matches)} lines match {key!r}")
            del lines[matches[0]]
            continue
        if len(matches) != 1:
            raise SystemExit(f"{name}: {len(matches)} lines match {key!r}")
        lines[matches[0]:matches[0] + 1] = new.split("\n")
    path.write_text("\n".join(lines))


def main():
    for name, edits in EDITS.items():
        edit_doc(name, edits)
    for name, fragments in FRAGMENTS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in fragments:
            text = text.replace(old, new)
        path.write_text(text)
    arch = DOCS / "ARCHITECTURE.md"
    text = arch.read_text()
    if ARCH_SENTENCE not in text:
        if text.count(ARCH_ANCHOR) != 1:
            raise SystemExit("ARCHITECTURE.md: anchor not found once")
        text = text.replace(ARCH_ANCHOR, ARCH_ANCHOR + ARCH_SENTENCE)
        arch.write_text(text)
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        body = path.read_text()
        stale += [f"{path.relative_to(ROOT)}: {word}" for word in REMOVED if re.search(re.escape(word), body)]
    if stale:
        raise SystemExit("still named:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
