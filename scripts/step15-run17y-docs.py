#!/usr/bin/env python3
"""Step 15, run 17y: the agent-docs entries of the "deals damage to a creature" trigger-condition records.

Each entry that named a removed record (or the "a creature you control" trigger with a record saying "this creature",
"equipped creature" or "a creature") is rewritten to the trigger with its filters and the existing effect that reads
the stack entry's triggering permanent; each removed record gets one "Gone" line where it was listed. Whole-line
edits keyed by a unique fragment of the old line. Idempotent: an edited line is left alone. Fails if a doc or skill
still names a removed record outside a "Gone" line.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

TRIG = ("`Triggers.dealsDamageToCreature()` (this creature), `equippedCreatureDealsDamageToCreature()`, "
        "`creatureYouControlDealsDamageToCreature()` or `creatureDealsDamageToCreature()` (any controller's), with "
        "`.withFilter(dealer)`, `.withDamagedFilter(damaged creature)` and `.withCombatDamageOnly()`")
GONE = "**Gone** (effect-slot-refactor.md step 15, run 17y): write " + TRIG

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`GrantTriggeredAbilityEffect(Triggers.creatureYouControlDealsDamageToCreature(), PutCountersOnSourceEffect(1,1,1), ALL_OWN_CREATURES)`",
         "| \"Each creature you control is a [subtype] in addition to its other creature types and has 'Whenever this creature deals damage to a creature, put a +1/+1 counter on this creature.'\" | `GrantSubtypeEffect(VAMPIRE, ALL_OWN_CREATURES)` + `GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.dealsDamageToCreature(), PutCountersOnSourceEffect(1,1,1)), ALL_OWN_CREATURES)` | STATIC + `Triggers.dealsDamageToCreature()` | Mephidross Vampire. The granted trigger is collected from the actual damage source, so each creature gets one trigger per damage event; it covers combat and noncombat damage. |"),
        ("| `DestroyDamagedCreatureEffect()` | `Triggers.creatureYouControlDealsDamageToCreature()` | Cruel Deceiver",
         "| \"Whenever this creature deals damage to a creature, destroy that creature\" | `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)` | `Triggers.dealsDamageToCreature()` | Cruel Deceiver grants this to itself. \"This creature\" is the trigger's relation; the creature dealt the damage is the stack entry's triggering permanent. \"Deals damage to a Vampire\": `.withDamagedFilter(new PermanentHasSubtypePredicate(VAMPIRE))` (Vampire Slayer, Dinosaur Hunter, Spider-Slayer) |"),
        ("| `DestroyDamagedCreatureEffect(true, true)` |",
         "| \"Whenever this creature deals combat damage to a creature, destroy that creature. It can't be regenerated.\" | `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)` | `Triggers.dealsDamageToCreature().withCombatDamageOnly()` | Grotesque Hybrid, Dripping Dead; non-targeting, the damaged creature is the entry's triggering permanent |"),
        ("| `ExileDamagedCreatureEffect()` |",
         "| \"Whenever equipped creature deals damage to a creature, exile that creature\" | `ExileTargetPermanentEffect.ofTriggeringPermanent()` | `Triggers.equippedCreatureDealsDamageToCreature()` | Sword of Kaldra (Pit Spawn's \"this creature\": `Triggers.dealsDamageToCreature()`). Non-targeting; the collector matches the Equipment's current or last-known attachment; the creature is exiled only if it is still on the battlefield |"),
        ("| `TapAndSkipUntapDamagedCreatureEffect()` |",
         "| \"Whenever this creature deals damage to a creature, tap that creature and it doesn't untap during its controller's next untap step\" | `TapPermanentsEffect(TapUntapScope.TRIGGERING)` + `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` | `Triggers.dealsDamageToCreature()` | Matsu-Tribe Sniper. Combat or non-combat damage; one non-targeting ability, the damaged creature its triggering permanent |"),
        ("| `DestroyDamagedCreatureAtEndOfCombatEffect(new PermanentHasSubtypePredicate(subtype))` |",
         "| \"Whenever a [subtype] you control deals combat damage to a creature, destroy that creature at end of combat\" | `DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent()` | `Triggers.creatureYouControlDealsDamageToCreature().withFilter(new PermanentHasSubtypePredicate(subtype)).withCombatDamageOnly()` | Sosuke, Son of Seshiro — the watcher counts as one of its own Warriors. \"Whenever this creature deals combat damage to a creature, destroy that creature at end of combat\": `Triggers.dealsDamageToCreature().withCombatDamageOnly()` (Stone-Tongue and Serpentine Basilisk, Ohran Viper, Simic Basilisk's grant) |"),
        ("| `DestroyCreatureDamagedByMatchingCreatureEffect(",
         "| \"Whenever a [subtype] deals combat damage to a creature, destroy that creature. It can't be regenerated.\" (any controller's source) | `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)` | `Triggers.creatureDealsDamageToCreature().withFilter(new PermanentHasSubtypePredicate(subtype)).withCombatDamageOnly()` | Toxin Sliver — the watcher controls the trigger even when an opponent's matching creature dealt the damage; do not model as a granted ability (a source that lost all abilities would stop triggering) |"),
        ("| `DestroyDamagedCreatureAtEndOfCombatEffect.thisCreature()` |",
         "| \"Whenever this creature deals damage to a creature, destroy that creature at end of combat\" | `DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent()` | `Triggers.dealsDamageToCreature()` | Lowland Basilisk — combat or noncombat damage; damage outside combat schedules destruction at the next end of combat |"),
        ("| `EquipmentDamagesOtherDefendingCreaturesEffect()` |",
         "| \"Whenever equipped creature deals damage to a blocking creature, this Equipment deals that much damage to each other creature defending player controls\" | `DealDamageToEachMatchingPermanentEffect(new EventValue(), AllOf(creature, Not(PermanentIsTriggeringPermanentPredicate), PermanentControlledByDefendingPlayerPredicate), ALL_PLAYERS)` | `Triggers.equippedCreatureDealsDamageToCreature().withDamagedFilter(new PermanentIsBlockingPredicate())` | Kusari-Gama. The Equipment is the source; the damaged blocker is the entry's triggering permanent, the damage its event value"),
        ("| `ReflectAllyDamageToDamagedCreatureControllerEffect(new PermanentHasSubtypePredicate(subtype))` |",
         "| \"whenever a [subtype] you control deals damage to a creature, that [subtype] deals that much damage to that creature's controller\" | `ReflectAllyDamageToDamagedCreatureControllerEffect()` | `Triggers.creatureYouControlDealsDamageToCreature().withFilter(new PermanentHasSubtypePredicate(subtype))` | Greatbow Doyen — fires for combat and non-combat damage; the creature that dealt the damage (not the watcher) deals the reflected damage (CR 608.2h), to the damaged creature's controller as the ability resolves. \"That creature's controller loses that much life\" of this creature's combat damage: `LoseLifeEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` on `Triggers.dealsDamageToCreature().withCombatDamageOnly()` (Flayed Nim) |"),
        ("| `DamageDamagedCreatureControllerAndSelfEffect(N, M)` |",
         "| \"whenever this creature deals damage to a creature, this creature deals N damage to that creature's controller and M damage to you\" | `DealDamageToPlayersEffect(N, TRIGGERING_PERMANENT_CONTROLLER)` + `DealDamageToPlayersEffect(M, CONTROLLER)` | `Triggers.dealsDamageToCreature()` | Bellowing Fiend — combat and non-combat damage; one ability, \"that creature's controller\" read as it resolves |"),
        ("you may put a quest counter on this enchantment\" | `MayEffect(PutCountersOnSelfEffect(QUEST))` | `Triggers.creatureYouControlDealsDamageToCreature()` |",
         "| \"Whenever a creature you control deals combat damage to a creature, you may put a quest counter on this enchantment\" | `MayEffect(PutCountersOnSelfEffect(QUEST))` | `Triggers.creatureYouControlDealsDamageToCreature().withCombatDamageOnly()` | Quest for the Gemblades — the enchantment is the ability's source; the \"you may\" is asked as the ability resolves |"),
        ("| `SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect()` |",
         "| \"Whenever this creature deals damage to a creature, if that creature was dealt excess damage this turn, that creature's controller sacrifices a noncreature, nonland permanent.\" | `SacrificePermanentsEffect(1, noncreature nonland, SacrificeRecipient.TARGET_PLAYER)` | `Triggers.creatureYouControlDealsDamageToCreature()` | Maarika, Brutal Gladiator. Its class-keyed collector handler judges \"this creature\" and the excess damage (this turn or by this event) and gives the entry the damaged creature's controller (effect-slot-refactor.md row 17y2 moves it to a trigger filter and an intervening \"if\"). |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect(CreateTokenEffect)` —",
         "- `CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect` — " + GONE + ", and the card's own effect: Giant's Skewer is `Triggers.equippedCreatureDealsDamageToCreature().withCombatDamageOnly()` + its Food `CreateTokenEffect`"),
        ("- `DestroyDamagedCreatureEffect()` / `(boolean combatDamageOnly, boolean cannotBeRegenerated)` —",
         "- `DestroyDamagedCreatureEffect` — " + GONE + " + `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING[, true])` (\"destroy that creature[. It can't be regenerated]\"; Cruel Deceiver's grant, Vampire Slayer's `.withDamagedFilter(Vampire)`, Grotesque Hybrid's `.withCombatDamageOnly()`)"),
        ("- `ExileDamagedCreatureEffect()` / `(true)` —",
         "- `ExileDamagedCreatureEffect` — " + GONE + " + `ExileTargetPermanentEffect.ofTriggeringPermanent()` (Pit Spawn's `dealsDamageToCreature()`, Sword of Kaldra's `equippedCreatureDealsDamageToCreature()`)"),
        ("- `TapAndSkipUntapDamagedCreatureEffect()` —",
         "- `TapAndSkipUntapDamagedCreatureEffect` — " + GONE + " + `TapPermanentsEffect(TapUntapScope.TRIGGERING)` + `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` (Matsu-Tribe Sniper)"),
        ("- `EquipmentDamagesOtherDefendingCreaturesEffect()` —",
         "- `EquipmentDamagesOtherDefendingCreaturesEffect` — " + GONE + ": Kusari-Gama is `equippedCreatureDealsDamageToCreature().withDamagedFilter(new PermanentIsBlockingPredicate())` + `DealDamageToEachMatchingPermanentEffect(new EventValue(), AllOf(creature, Not(PermanentIsTriggeringPermanentPredicate), PermanentControlledByDefendingPlayerPredicate), ALL_PLAYERS)`"),
        ("- `EquipmentTapsAndLocksDamagedCreatureEffect()` —",
         "- `EquipmentTapsAndLocksDamagedCreatureEffect` — " + GONE + ": Neko-Te is `equippedCreatureDealsDamageToCreature()` + `TapPermanentsEffect(TapUntapScope.TRIGGERING)` + `DoesntUntapEffect.triggeringWhileSourceOnBattlefield()`"),
        ("- `DestroyDamagedCreatureAtEndOfCombatEffect(PermanentPredicate sourceFilter)` — `Triggers.creatureYouControlDealsDamageToCreature()` marker, **combat damage only**: whenever a creature you control matching `sourceFilter` deals combat damage to a creature, destroy that creature at end of combat. Unlike",
         "- `DestroyDamagedCreatureAtEndOfCombatEffect` — " + GONE + " + `DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent()` (Sosuke's `creatureYouControlDealsDamageToCreature().withFilter(Warrior).withCombatDamageOnly()`, the basilisks' `dealsDamageToCreature()[.withCombatDamageOnly()]`)"),
        ("- `DestroyDamagedCreatureAtEndOfCombatEffect(PermanentPredicate sourceFilter)` — `Triggers.creatureYouControlDealsDamageToCreature()` marker, **combat damage only** by default",
         None),
        ("- `DestroyCreatureDamagedByMatchingCreatureEffect(PermanentPredicate sourceFilter, boolean combatDamageOnly, boolean cannotBeRegenerated)` —",
         "- `DestroyCreatureDamagedByMatchingCreatureEffect` — " + GONE + ": Toxin Sliver is `creatureDealsDamageToCreature().withFilter(Sliver).withCombatDamageOnly()` + `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)`; the watcher controls the ability whoever controls the dealer"),
        ("- `PutCountersOnSourceEffect(int power, int toughness, int amount)` — counters on self; it can also be granted through `GrantTriggeredAbilityEffect` for a creature's `Triggers.creatureYouControlDealsDamageToCreature()` trigger",
         "- `PutCountersOnSourceEffect(int power, int toughness, int amount)` — counters on self; it can also be granted through `GrantTriggeredAbilityEffect` for a creature's `Triggers.dealsDamageToCreature()` trigger (\"whenever this creature deals damage to a creature\")"),
        ("`SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect()` —",
         "`SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` — **Gone** (effect-slot-refactor.md step 15, run 17y; no card wrote it): Maarika, Brutal Gladiator writes `SacrificePermanentsEffect(1, noncreature nonland, TARGET_PLAYER)` on `Triggers.creatureYouControlDealsDamageToCreature()`, collected by its class-keyed handler until row 17y2"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `DestroyDamagedCreatureEffect` | `()` / `(PermanentPredicate damagedCreatureFilter)`",
         "| `DestroyDamagedCreatureEffect` | — | " + GONE + " + `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING[, true])` |"),
        ("| `ExileDamagedCreatureEffect` | `()` or `(boolean equipmentScoped)`",
         "| `ExileDamagedCreatureEffect` | — | " + GONE + " + `ExileTargetPermanentEffect.ofTriggeringPermanent()` |"),
        ("| `CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect` | `(CreateTokenEffect token)`",
         "| `CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect` | — | " + GONE + " + the `CreateTokenEffect` (Giant's Skewer) |"),
        ("| `TapAndSkipUntapDamagedCreatureEffect` | `()`",
         "| `TapAndSkipUntapDamagedCreatureEffect` | — | " + GONE + " + `TapPermanentsEffect(TapUntapScope.TRIGGERING)` + `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` |"),
        ("| `DestroyDamagedCreatureAtEndOfCombatEffect` | `(PermanentPredicate sourceFilter)`",
         "| `DestroyDamagedCreatureAtEndOfCombatEffect` | — | " + GONE + " + `DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent()` |"),
        ("| `DestroyCreatureDamagedByMatchingCreatureEffect` | `(PermanentPredicate sourceFilter, boolean combatDamageOnly, boolean cannotBeRegenerated)`",
         "| `DestroyCreatureDamagedByMatchingCreatureEffect` | — | " + GONE + " + `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)` (Toxin Sliver: `creatureDealsDamageToCreature().withFilter(Sliver).withCombatDamageOnly()`) |"),
        ("| `ReflectAllyDamageToDamagedCreatureControllerEffect` | `(PermanentPredicate sourceFilter)`; `selfCombatDamageCausesLifeLoss()`",
         "| `ReflectAllyDamageToDamagedCreatureControllerEffect` | `()` | trigger-condition record on `Triggers.creatureYouControlDealsDamageToCreature().withFilter(…)` (Greatbow Doyen): the creature that dealt the damage deals that much damage to the damaged creature's controller (CR 608.2h), so its class-keyed collector handler puts the record's `triggeredEffect()` (`DealDamageToPlayersEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)`) on the stack with that creature as the entry's source, the damaged creature as its triggering permanent and the damage as its event value (row 17y2 of effect-slot-refactor.md). Flayed Nim's former self combat life-loss form is `LoseLifeEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` on `Triggers.dealsDamageToCreature().withCombatDamageOnly()` |"),
        ("| `DamageDamagedCreatureControllerAndSelfEffect` | `(int amountToDamagedCreatureController, int amountToSelf)`",
         "| `DamageDamagedCreatureControllerAndSelfEffect` | — | " + GONE + " + `DealDamageToPlayersEffect(N, TRIGGERING_PERMANENT_CONTROLLER)` + `DealDamageToPlayersEffect(M, CONTROLLER)` (Bellowing Fiend) |"),
        ("| `EquipmentDamagesOtherDefendingCreaturesEffect` | `()`",
         "| `EquipmentDamagesOtherDefendingCreaturesEffect` | — | " + GONE + " + `DealDamageToEachMatchingPermanentEffect(new EventValue(), AllOf(creature, Not(PermanentIsTriggeringPermanentPredicate), PermanentControlledByDefendingPlayerPredicate), ALL_PLAYERS)` (Kusari-Gama, `.withDamagedFilter(new PermanentIsBlockingPredicate())`) |"),
        ("| `EquipmentTapsAndLocksDamagedCreatureEffect` | `()`",
         "| `EquipmentTapsAndLocksDamagedCreatureEffect` | — | " + GONE + " + `TapPermanentsEffect(TapUntapScope.TRIGGERING)` + `DoesntUntapEffect.triggeringWhileSourceOnBattlefield()` (Neko-Te) |"),
        ("also usable as a continuously granted `Triggers.creatureYouControlDealsDamageToCreature()` self-trigger |",
         "| `PutCountersOnSourceEffect` | `(int powerModifier, int toughnessModifier, int amount)` | put N counters on this creature (e.g. `(1,1,1)` for +1/+1, `(-1,-1,2)` for two -1/-1); also usable as a continuously granted `Triggers.dealsDamageToCreature()` self-trigger |"),
        ("| `SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` | `()`",
         "| `SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17y; no card wrote it): Maarika writes `SacrificePermanentsEffect(1, noncreature nonland, TARGET_PLAYER)` on `Triggers.creatureYouControlDealsDamageToCreature()` |"),
        ("| `DestroyTargetPermanentAtEndOfCombatEffect` | `()` or `(boolean cannotBeRegenerated)` |",
         "APPEND: `DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent()` schedules the stack entry's triggering permanent instead and targets nothing (\"whenever this creature deals combat damage to a creature, destroy that creature at end of combat\": the basilisks, Sosuke, Son of Seshiro)."),
        ("| `DoesntUntapEffect` | build via static factories",
         "APPEND: `DoesntUntapEffect.triggeringWhileSourceOnBattlefield()` (`TRIGGERING` + `WHILE_SOURCE_ON_BATTLEFIELD`) locks the stack entry's triggering permanent while the source stays on the battlefield (Neko-Te's \"that creature doesn't untap … for as long as this Equipment remains on the battlefield\")."),
        ("| `ExileTargetPermanentEffect` | `()`, `(CreateTokenEffect tokenForController)`, or `(PermanentPredicate targetFilter)` |",
         "APPEND: `ExileTargetPermanentEffect.ofTriggeringPermanent()` exiles the stack entry's triggering permanent instead, if it is still on the battlefield, and targets nothing (\"exile that creature\" of a damage trigger: Pit Spawn, Sword of Kaldra)."),
    ],
    "EFFECTS_INDEX.md#2": [
        ("Cruel Deceiver (`Triggers.creatureYouControlDealsDamageToCreature()` + `DestroyDamagedCreatureEffect()`) |",
         "APPEND-REPLACE: Cruel Deceiver (`Triggers.creatureYouControlDealsDamageToCreature()` + `DestroyDamagedCreatureEffect()`) |=>Cruel Deceiver (`new TriggeredAbility(Triggers.dealsDamageToCreature(), new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING))`) |"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Subtype lord (own) + damage reflection | `g/GreatbowDoyen.java` |",
         "| Subtype lord (own) + damage reflection | `g/GreatbowDoyen.java` | STATIC StaticBoostEffect(1, 1, OWN_CREATURES, PermanentHasSubtypePredicate(ARCHER)) + Triggers.creatureYouControlDealsDamageToCreature().withFilter(PermanentHasSubtypePredicate(ARCHER)) ReflectAllyDamageToDamagedCreatureControllerEffect() — +1/+1 to other Archers you control, and whenever an Archer you control deals damage to a creature that Archer deals that much to that creature's controller |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| Boost + combat-damage-to-creature Food trigger equip | `g/GiantsSkewer.java` |",
         "| Boost + combat-damage-to-creature Food trigger equip | `g/GiantsSkewer.java` | STATIC StaticBoostEffect(2, 1, EQUIPPED_CREATURE) + Triggers.equippedCreatureDealsDamageToCreature().withCombatDamageOnly() CreateTokenEffect(Food) + EquipActivatedAbility(`{3}`) |"),
        ("| Granted pump + damage-splash-on-blocker equip | `k/KusariGama.java` |",
         "| Granted pump + damage-splash-on-blocker equip | `k/KusariGama.java` | STATIC GrantActivatedAbilityEffect(ActivatedAbility(\"{2}\", BoostSelfEffect(1, 0)), EQUIPPED_CREATURE) + Triggers.equippedCreatureDealsDamageToCreature().withDamagedFilter(PermanentIsBlockingPredicate) DealDamageToEachMatchingPermanentEffect(EventValue, other creature defending player controls, ALL_PLAYERS) + EquipActivatedAbility(\"{3}\"). The trigger lives on the Equipment and still fires when the equipped creature dies to the same combat damage (last-known attachment) |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Continuous creature-type and self-counter trigger grant | `m/MephidrossVampire.java` |",
         "| Continuous creature-type and self-counter trigger grant | `m/MephidrossVampire.java` | STATIC `GrantSubtypeEffect(VAMPIRE, ALL_OWN_CREATURES)` + `GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.dealsDamageToCreature(), PutCountersOnSourceEffect(1,1,1)), ALL_OWN_CREATURES)`; the damage collector reads the granted trigger of the creature that dealt the damage |"),
        ("| Combat or noncombat damage → tap and lock the damaged creature | `m/MatsuTribeSniper.java` |",
         "| Combat or noncombat damage → tap and lock the damaged creature | `m/MatsuTribeSniper.java` | `Triggers.dealsDamageToCreature()` + `TapPermanentsEffect(TapUntapScope.TRIGGERING)` + `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` — the damaged creature is the stack entry's triggering permanent |"),
        ("| Whenever this creature deals damage to a creature: exile that creature | `p/PitSpawn.java` |",
         "| Whenever this creature deals damage to a creature: exile that creature | `p/PitSpawn.java` | `Triggers.dealsDamageToCreature()` + `ExileTargetPermanentEffect.ofTriggeringPermanent()` — combat and noncombat; non-targeting |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.creatureYouControlDealsDamageToCreature()` | A creature you control (matching the effect's source filter)",
         "| `Triggers.creatureYouControlDealsDamageToCreature()` (and `dealsDamageToCreature()`, `equippedCreatureDealsDamageToCreature()`, `creatureDealsDamageToCreature()`) | A creature you control (this creature, equipped creature, any creature) deals damage — combat or non-combat — to a creature; same layer-aware creature gate on the *damaged* permanent as the two slots above. `.withFilter` narrows the creature that deals it, `.withDamagedFilter` the creature dealt it, `.withCombatDamageOnly()` the damage; the damaged creature is the stack entry's triggering permanent and the damage its event value. Greatbow Doyen (`.withFilter(Archer)` + `ReflectAllyDamageToDamagedCreatureControllerEffect()`) |"),
    ],
}

RECORDS = ["TapAndSkipUntapDamagedCreatureEffect", "DestroyDamagedCreatureAtEndOfCombatEffect",
           "EquipmentTapsAndLocksDamagedCreatureEffect", "DamageDamagedCreatureControllerAndSelfEffect",
           "DestroyCreatureDamagedByMatchingCreatureEffect", "DamagedCreatureTriggerEffect",
           "SacrificeNoncreatureNonlandPermanentIfExcessDamageEffect", "ExileDamagedCreatureEffect",
           "DestroyDamagedCreatureEffect", "CreateTokenWhenEquippedCreatureDealsCombatDamageToCreatureEffect",
           "EquipmentDamagesOtherDefendingCreaturesEffect", "selfCombatDamageCausesLifeLoss"]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name.split("#")[0]
        lines = path.read_text().split("\n")
        for key, replacement in edits:
            hits = [i for i, line in enumerate(lines) if key in line]
            if replacement is None:
                lines = [line for i, line in enumerate(lines) if i not in hits]
                continue
            if replacement.startswith("APPEND-REPLACE: "):
                old, new = replacement[len("APPEND-REPLACE: "):].split("=>")
                for i in hits:
                    lines[i] = lines[i].replace(old, new)
                continue
            if replacement.startswith("APPEND: "):
                extra = replacement[len("APPEND: "):]
                for i in hits:
                    if extra not in lines[i]:
                        lines[i] = lines[i].rstrip()
                        lines[i] = (lines[i][:-1].rstrip() + " " + extra + " |") if lines[i].endswith("|") \
                            else lines[i] + " " + extra
                continue
            if not hits:
                if replacement not in lines:
                    raise SystemExit(f"{name}: no line has {key!r}")
                continue
            if len(hits) > 1:
                raise SystemExit(f"{name}: {len(hits)} lines have {key!r}")
            lines[hits[0]] = replacement
        path.write_text("\n".join(lines))
    leftovers = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        if path.name in ("EFFECT_MERGE_PLAN.md", "TARGET_PREDICATE_PLAN.md"):
            continue
        for number, line in enumerate(path.read_text().split("\n"), 1):
            for record in RECORDS:
                if re.search(r"\b" + record + r"\b", line) and "**Gone**" not in line:
                    leftovers.append(f"{path.name}:{number}: {record}")
    if leftovers:
        raise SystemExit("still named:\n" + "\n".join(leftovers))


if __name__ == "__main__":
    main()
