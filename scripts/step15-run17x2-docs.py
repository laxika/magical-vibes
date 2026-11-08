#!/usr/bin/env python3
"""Step 15, run 17x2: the agent docs for the damage family's abilities that read the stack entry.

Rewrites the doc fragments naming a form the run changed: the trigger-condition records whose collectors bound the
damage, the source or the damaged creature into the effect (`ReflectAllyDamageToDamagedCreatureControllerEffect`,
`EquipmentDamagesOtherDefendingCreaturesEffect`, `ReflectSourceDamageToItsControllerEffect`,
`ReflectDamageToChosenColorCreatureEffect`, `DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect`:
each now names the `triggeredEffect()` that reads the entry), the removed `DealDamageToDamageSourceControllerEffect`
(Flameblade Angel's `DealDamageToPlayersEffect(1, TRIGGERING_PERMANENT_CONTROLLER)`), "destroy it" of an enchanted or
equipped creature dealt damage (`PermanentReference.TRIGGERING`), All Will Be One's `EventValue`, Phage's
`TargetPlayerLosesGameEffect(null)` and Harsh Justice's reflection.

Exact fragment edits: a fragment already rewritten is skipped, so the script is idempotent. It fails if an edit's old
fragment and new fragment are both missing, or if a doc or skill still names the removed effect.

Usage: step15-run17x2-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "EFFECTS_INDEX.md": [
        ("then deals that amount to each opponent (Imodane, the Pyrohammer) |",
         "then puts its `triggeredEffect()`, `DealDamageToPlayersEffect(new EventValue(), EACH_OPPONENT)`, on the stack with "
         "that amount as the entry's event value (Imodane, the Pyrohammer) |"),
        ("The collector expands the marker at trigger time, preserving the damaged controller even if the creature leaves "
         "the battlefield. |",
         "The collector puts the record's `triggeredEffect()` on the stack (`DealDamageToPlayersEffect` / `LoseLifeEffect` "
         "of `new EventValue()` to `TRIGGERING_PERMANENT_CONTROLLER`) with the damage-source creature as the entry's source, "
         "the damaged creature as its triggering permanent (its controller as the ability resolves, or as it last existed) "
         "and the damage as its event value. |"),
        ("expanding it into a `DealDamageToEachMatchingPermanentEffect(damage, PermanentNotPredicate(PermanentIsSpecificPermanentPredicate(damagedCreatureId)), "
         "TARGET_PLAYER)` entry sourced from the Equipment, so the marker itself is never resolved |",
         "putting its `triggeredEffect()`, `DealDamageToEachMatchingPermanentEffect(new EventValue(), creature and "
         "PermanentNotPredicate(PermanentIsTriggeringPermanentPredicate()), TARGET_PLAYER)`, on the stack sourced from the "
         "Equipment, with the defending player as its non-targeting player, the damaged blocker as its triggering "
         "permanent and the damage as its event value, so the record itself is never resolved |"),
        ("The collector translates each match into a `DealDamageToTargetCreatureEffect(amount)` with the damaging creature "
         "baked in as a non-chosen target, so the marker itself is never resolved. Used by Mangara's Equity |",
         "The collector puts the record's `triggeredEffect()`, `DealDamageToTargetCreatureEffect(new EventValue())`, on "
         "the stack with the damaging creature as a non-chosen target and the damage as the entry's event value, so the "
         "record itself is never resolved. Used by Mangara's Equity |"),
        ("The collector translates each match into a `DealDamageToPlayersEffect(total, TARGET_PLAYER)` dealt by the "
         "watcher, so the marker itself is never resolved |",
         "The collector puts the record's `triggeredEffect()`, `DealDamageToPlayersEffect(new EventValue(), "
         "TRIGGERING_PERMANENT_CONTROLLER)`, on the stack, dealt by the watcher, with the total as the entry's event value "
         "and the source as its triggering permanent: a creature's controller as the ability resolves, a spell's as it "
         "dealt the damage. The record itself is never resolved |"),
        ("| `DealDamageToDamageSourceControllerEffect` | `(int amount)` or `(int amount, UUID damageSourcePermanentId, "
         "UUID damageSourceControllerId)` | deals damage to the controller of the source that caused a trigger; the bound "
         "permanent controller is rechecked on resolution and the trigger-time controller is used if the source permanent "
         "is no longer present. Used by Flameblade Angel |\n", ""),
        ("Pass a pre-resolved UUID for triggers that already know the player (Phage the Untouchable); pass `null` for "
         "spells / activated abilities and the handler uses the stack entry's chosen target (`targetSpec` = harmful "
         "`PLAYER`, e.g. Door to Nothingness) |",
         "Pass `null` on cards: the handler uses the stack entry's player, the chosen target for spells / activated "
         "abilities (`targetSpec` = harmful `PLAYER`, e.g. Door to Nothingness), or the player dealt combat damage for a "
         "`Triggers.dealsCombatDamageToPlayer()` ability (\"that player loses the game\", Phage the Untouchable; the "
         "collector gives the entry that player as its non-targeting player through `DAMAGED_PLAYER`). A pre-resolved "
         "UUID is for engine-built abilities only |"),
        ("it deals that much damage back to its controller (the attacking player), with the attacking creature as the "
         "damage source.",
         "it deals that much damage back to its controller, with the attacking creature as the damage source and the "
         "entry's triggering permanent (`DealDamageToPlayersEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)`, "
         "the damage its event value)."),
        ("`Triggers.enchantedCreatureDealtDamage()`: Mortal Wound / Hot Soup; ", ""),
        ("(\"destroy it\" for the permanent that entered — Suleiman's Legacy on",
         "(\"destroy it\" for the creature an enchanted / equipped creature trigger was about — Mortal Wound, Hot Soup, "
         "Mire Blight, Sporogenic Infection, Cryoshatter on `Triggers.enchantedCreatureDealtDamage()`, so moving the Aura "
         "or Equipment in response changes nothing; for the permanent that entered — Suleiman's Legacy on"),
        ("`DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect`, `DealDamageToDamageSourceControllerEffect`, "
         "`DealDamageToTargetCreatureEffect`, ",
         "`DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect`, `DealDamageToTargetCreatureEffect`, "),
    ],
    "ARCHITECTURE.md": [
        ("Only a graveyard target whose restriction reads the damage is narrowed at the damage as it is chosen, because no "
         "stack entry exists yet.",
         "Only a graveyard target whose restriction reads the damage is narrowed at the damage as it is chosen, because no "
         "stack entry exists yet. The same holds for the other damage collectors: \"that much\" is the entry's event value "
         "(a choice carries it, `SpellTargetTriggerAnyTarget.spellManaSpentX`), and \"that creature's controller\" or "
         "\"that source's controller\" the controller of the entry's triggering permanent as the ability resolves "
         "(`TRIGGERING_PERMANENT_CONTROLLER`, the controller it had as the event happened its fallback, a spell source's "
         "too); no collector builds the effect with the damage, the player or the creature bound in. A trigger-condition "
         "record a collector judges by class (Justice, Greatbow Doyen, Kusari-Gama, Mangara's Equity, Imodane) names the "
         "effect that goes on the stack as its `triggeredEffect()`."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("`CombatDamageService` reflects each attacking creature's player-damage back to its controller.",
         "`CombatDamageService` reflects each attacking creature's player-damage back to its controller "
         "(`DealDamageToPlayersEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)`, the creature the entry's "
         "triggering permanent)."),
        ("Expanded at trigger-collection time into a `DealDamageToEachMatchingPermanentEffect(…, TARGET_PLAYER)` entry "
         "that excludes the damaged blocker, so it never resolves directly. Kusari-Gama",
         "Its `triggeredEffect()` goes on the stack: `DealDamageToEachMatchingPermanentEffect(new EventValue(), …, "
         "TARGET_PLAYER)` excluding the entry's triggering permanent (the damaged blocker), so it never resolves directly. "
         "Kusari-Gama"),
        ("and deals the actual damage recorded against that creature to each opponent",
         "and puts its `triggeredEffect()` (`DealDamageToPlayersEffect(new EventValue(), EACH_OPPONENT)`) on the stack "
         "with the actual damage recorded against that creature as the event value"),
        ("the source deals that much damage back to that creature (Mangara's Equity). Pair with `ChooseColorOnEnterEffect`",
         "the source deals that much damage back to that creature (Mangara's Equity; `DealDamageToTargetCreatureEffect(new "
         "EventValue())` on the stack). Pair with `ChooseColorOnEnterEffect`"),
        ("this permanent deals that much to that source's controller (Justice). Damage from one source is summed into one "
         "reflection (CR ruling)",
         "this permanent deals that much to that source's controller (Justice): a creature's controller as the ability "
         "resolves, a spell's as it dealt the damage. Damage from one source is summed into one reflection (CR ruling)"),
        ("Mortal Wound / Hot Soup on `Triggers.enchantedCreatureDealtDamage()`, ", ""),
        ("put it on `Triggers.enters(PermanentAnyOfPredicate(PermanentHasSubtypePredicate(DJINN), "
         "PermanentHasSubtypePredicate(EFREET)), ObjectRelation.ANY)` (Suleiman's Legacy",
         "\"destroy it\" of `Triggers.enchantedCreatureDealtDamage()` is the creature dealt damage (Mortal Wound, Hot Soup, "
         "Mire Blight, Sporogenic Infection, Cryoshatter); put it on `Triggers.enters(PermanentAnyOfPredicate("
         "PermanentHasSubtypePredicate(DJINN), PermanentHasSubtypePredicate(EFREET)), ObjectRelation.ANY)` (Suleiman's Legacy"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("the collector queues one non-targeting trigger with the target's actual damage amount |",
         "the collector queues one non-targeting trigger whose event value is the target's actual damage amount |"),
        ("then queues `DealDamageToEachMatchingPermanentEffect` scoped to the defending player and excluding that blocker",
         "then queues the record's `DealDamageToEachMatchingPermanentEffect(new EventValue(), …)` scoped to the defending "
         "player and excluding the entry's triggering permanent (that blocker)"),
        ("| `DestroyReferencedPermanentEffect(PermanentReference.ATTACHED)` | `Triggers.enchantedCreatureDealtDamage()` | "
         "Mortal Wound — collector bakes the Aura's source-permanent id so the enchanted creature is re-derived at resolution |",
         "| `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)` | `Triggers.enchantedCreatureDealtDamage()` | "
         "Mortal Wound, Hot Soup — \"it\" is the creature dealt damage, the entry's triggering permanent, even if the Aura "
         "or Equipment moves before the ability resolves |"),
        ("Greatbow Doyen — fires for combat and non-combat damage; the source creature (not the watcher) deals the "
         "reflected damage |",
         "Greatbow Doyen — fires for combat and non-combat damage; the source creature (not the watcher) deals the "
         "reflected damage, to the damaged creature's controller as the ability resolves |"),
        ("dealt by the watcher to the source's controller. Parameterize",
         "dealt by the watcher to the source's controller (a creature's as the ability resolves). Parameterize"),
        ("dealing that much back to its controller with the attacking creature as the damage source. Cleared",
         "dealing that much (its event value) back to its controller as it resolves with the attacking creature as the "
         "damage source. Cleared"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`MayEffect(DealDamageToDamageSourceControllerEffect(1))` — fires once per opponent-controlled source per damage "
         "event, including damage to controlled permanents |",
         "`MayEffect(DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER))` — fires once per "
         "opponent-controlled source per damage event, including damage to controlled permanents; the source is the "
         "entry's triggering permanent, so \"that source's controller\" is read as the ability resolves |"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.enchantedCreatureDealtDamage() `DestroyReferencedPermanentEffect(ATTACHED)` — any damage",
         "Triggers.enchantedCreatureDealtDamage() `DestroyReferencedPermanentEffect(TRIGGERING)` — any damage"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("`DestroyReferencedPermanentEffect(ATTACHED)` (Mortal Wound)",
         "`DestroyReferencedPermanentEffect(TRIGGERING)` (Mortal Wound: the creature dealt damage)"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("event caused by the controller, including poison counters, and uses the spell-target trigger pipeline),",
         "event caused by the controller, including poison counters, and uses the spell-target trigger pipeline; \"that "
         "much damage\" is `DealDamageToAnyTargetEffect(new EventValue())`, the counters put the choice's event value),"),
    ],
}

REMOVED = ("DealDamageToDamageSourceControllerEffect",)


def main():
    failed = False
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        original = text
        for old, new in edits:
            if new and new in text:
                continue
            if old in text:
                text = text.replace(old, new, 1)
            elif not new:
                continue
            else:
                print(f"{name}: fragment not found: {old[:90]}")
                failed = True
        if text != original:
            path.write_text(text)
            print(f"updated {name}")
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        for removed in REMOVED:
            if removed in path.read_text():
                print(f"{path.relative_to(ROOT)} still names {removed}")
                failed = True
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
