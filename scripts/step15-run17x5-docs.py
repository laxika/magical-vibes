#!/usr/bin/env python3
"""Step 15, run 17x5: agent-docs edits for the forms the run changed or removed.

Each edit is (file, old, new): `old` is replaced by `new`; a file that already has `new` is left alone, and a missing
`old` fails. A row to drop has an empty `new` and is left alone once gone. Afterwards the script fails if a doc or skill
still names a removed form. Idempotent.

Usage: step15-run17x5-docs.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
D = "agent-docs/"

EDITS = [
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "crew trigger marker that filters the crewing creature, then binds the crewed Vehicle's physical card to a perpetual P/T boost",
     "crew trigger-condition record that filters the crewing creature; its `triggeredEffect()` is "
     "`PerpetuallyBoostCardEffect.ofTriggeringPermanent(p, t)`, the crewed Vehicle the entry's triggering permanent"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "- `ReturnCardsExiledWithSourceOnUntapEffect()` — return the linked cards captured when the source's untap trigger fired.",
     "- `ReturnCardsExiledWithSourceOnUntapEffect()` — return the linked cards exiled with the source as its untap "
     "trigger fired (the stack entry's triggering cards)."),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "The leave-trigger collector freezes dynamic power/toughness against the source's last-known state; resolution",
     "Dynamic power/toughness is evaluated as it resolves against the source's last-known state (the entry's source "
     "snapshot); resolution"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "- `CreateTokensForExiledCardsWithSourceEffect(CreateTokenEffect tokenTemplate[, UUID sourcePermanentId])`",
     "- `CreateTokensForExiledCardsWithSourceEffect(CreateTokenEffect tokenTemplate)`"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "The leave-trigger collector bakes the departed source ID into the effect; cards no longer in exile do not count",
     "The source is the stack entry's source permanent, kept after it left; cards no longer in exile do not count"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "- `DestroyEnchantedCreatureOnLeaveEffect()` / `(UUID enchantedPermanentId, boolean cannotBeRegenerated)` — "
     "`Triggers.leavesBattlefield(THIS)`: destroy the creature the source Aura enchanted. The collector captures the "
     "host's permanent ID before the Aura disappears; use the card-definition constructor for the default "
     "no-regeneration form",
     "- `DestroyEnchantedCreatureOnLeaveEffect()` / `(boolean cannotBeRegenerated)` — "
     "`Triggers.leavesBattlefield(THIS)`: destroy the creature the source Aura enchanted, the host of the Aura as it "
     "last existed (the entry's source snapshot), read as it resolves; the no-argument form can't be regenerated"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "The collector bakes the leaving permanent's id in; each card exiled with that source",
     "The source is the stack entry's source permanent, kept after it left; each card exiled with that source"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "On the leaves slot a dedicated trigger collector bakes the linked id into the effect (not the entry's "
     "`targetId`, which would fizzle against a spell target filter); on the untap slot the id stays null and the "
     "handler reads it off the still-present source.",
     "The handler reads the link as it resolves: off the still-present source (untap), else off the source as it "
     "last existed (the entry's source snapshot, leaves)."),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "the enchanted creature's permanent id is baked in at trigger time so it resolves after the Aura is gone.",
     "\"that creature\" is the Aura's host as it last existed (the entry's source snapshot), read as it resolves "
     "after the Aura is gone."),
    (D + "EFFECTS_INDEX.md",
     "trigger-only `Triggers.creatureYouControlCrewsVehicle()` effect; when the event's crewing creature matches, it "
     "binds the Vehicle's physical card to `PerpetuallyBoostCardEffect`.",
     "trigger-condition record on `Triggers.creatureYouControlCrewsVehicle()`; when the event's crewing creature "
     "matches, its `triggeredEffect()` (`PerpetuallyBoostCardEffect.ofTriggeringPermanent`) goes on the stack with the "
     "Vehicle as the entry's triggering permanent."),
    (D + "EFFECTS_INDEX.md",
     "| `ReturnCardsExiledWithSourceOnUntapEffect` | `()` | returns the linked cards captured when Tawnos's Coffin's "
     "untap trigger fired |",
     "| `ReturnCardsExiledWithSourceOnUntapEffect` | `()` | returns the linked cards exiled with Tawnos's Coffin as its "
     "untap trigger fired (the stack entry's triggering cards) |"),
    (D + "EFFECTS_INDEX.md",
     "The collector supplies the source id because the source is already gone when the trigger resolves.",
     "The source is the stack entry's source permanent, kept after it left; the id form is for engine-built entries."),
    (D + "EFFECTS_INDEX.md",
     "| `DestroyEnchantedCreatureOnLeaveEffect` | `()` / `(UUID enchantedPermanentId)` / `(UUID enchantedPermanentId, "
     "boolean cannotBeRegenerated)` | `Triggers.leavesBattlefield(THIS)`: destroy the creature the source Aura "
     "enchanted. The enchanted creature's permanent id is baked in at trigger time because the Aura is gone by "
     "resolution; the default card-definition form prevents regeneration.",
     "| `DestroyEnchantedCreatureOnLeaveEffect` | `()` / `(boolean cannotBeRegenerated)` | "
     "`Triggers.leavesBattlefield(THIS)`: destroy the creature the source Aura enchanted: the Aura's host as it last "
     "existed (the entry's source snapshot), read as it resolves; the no-argument form prevents regeneration."),
    (D + "EFFECTS_INDEX.md",
     "| `MayCastCardsExiledWithSourceEffect` | `(UUID sourcePermanentId)` / `()` |",
     "| `MayCastCardsExiledWithSourceEffect` | `()` |"),
    (D + "EFFECTS_INDEX.md",
     "the card-level instance leaves the id null and `DeathTriggerCollectorService` bakes the leaving permanent's id "
     "in, since the source is already gone when the trigger resolves.",
     "the source is the stack entry's source permanent, kept after it left."),
    (D + "EFFECTS_INDEX.md",
     "The enchanted creature's permanent id is baked in at trigger time (captured before the Aura is gone), so it "
     "works even though `sourcePermanentId` no longer resolves.",
     "\"That creature\" is the Aura's host as it last existed (the entry's source snapshot), read as it resolves after "
     "the Aura is gone."),
    (D + "EFFECTS_INDEX.md",
     "`(int count, GraveyardExileScope scope[, CardPredicate filter[, UUID affectedPlayerId]])`",
     "`(int count, GraveyardExileScope scope[, CardPredicate filter])`"),
    (D + "EFFECTS_INDEX.md",
     "The leave-trigger collector freezes dynamic power/toughness; the normal handler",
     "Dynamic power/toughness is evaluated as it resolves against the source as it last existed; the handler"),
    (D + "EFFECTS_INDEX.md",
     "| `CreateTokensForExiledCardsWithSourceEffect` | `(CreateTokenEffect tokenEffect[, UUID sourcePermanentId])` |",
     "| `CreateTokensForExiledCardsWithSourceEffect` | `(CreateTokenEffect tokenEffect)` |"),
    (D + "EFFECTS_INDEX.md",
     "The leave collector bakes the departed source ID into the effect; the normal handler",
     "The source is the stack entry's source permanent, kept after it left; the handler"),
    (D + "EFFECTS_INDEX.md",
     "Card-authored form uses the single-arg ctor; the dedicated trigger collector in `DeathTriggerCollectorService` "
     "reads the leaving permanent's `chosenPermanentId`, bakes it into a fresh effect with the linked id set (NOT the "
     "stack entry's `targetId`, which resolution would validate against the source card's spell target filter and "
     "fizzle), and drops the trigger when the bond is absent.",
     "Card-authored form uses the single-arg ctor; on the leaves slot the handler reads the link off the source as it "
     "last existed (the entry's source snapshot), and the ability doesn't trigger when the bond is absent; the "
     "two-arg form names the linked permanent for an engine-built delayed trigger."),
    (D + "EFFECTS_INDEX.md",
     "the collector queues its `triggeredEffect()` (`DealDamageToAnyTargetEffect(damage)`) through "
     "`LifeGainTriggerAnyTarget`.",
     "the collector queues its `triggeredEffect()` (`DealDamageToTargetCreatureOrPlayerEffect(damage)`: \"target "
     "creature or player\", no planeswalker or battle) through `LifeGainTriggerAnyTarget`."),
    (D + "EFFECTS_INDEX.md",
     "| `ControllerLosesGameOnLeavesEffect` | `()` | \"When this permanent leaves the battlefield, you lose the game.\" "
     "Fires on any departure (destroy, exile, bounce, sacrifice, tuck). Slot: `Triggers.leavesBattlefield(THIS)` |\n",
     ""),
    (D + "EFFECTS_INDEX.md",
     "| `DestroyMerfolkTappedForSourceAbilitiesEffect` | `()` | `Triggers.dies(THIS)` descriptor for Vodalian War "
     "Machine. The death collector snapshots the source's Merfolk ids tapped to pay for its abilities and queues a "
     "regular `DestroyAllPermanentsEffect` narrowed to those ids and the Merfolk subtype. |\n",
     ""),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "`BoostSelfEffect(2, 1)` + `DestroyMerfolkTappedForSourceAbilitiesEffect` in `Triggers.dies(THIS)` | ACTIVATED + "
     "Triggers.dies(THIS) | Vodalian War Machine. The tracked Merfolk ids are snapshotted when the source dies, then "
     "resolved through a non-targeting `DestroyAllPermanentsEffect` filter",
     "`BoostSelfEffect(2, 1)` + `DestroyAllPermanentsEffect(PermanentAllOfPredicate(merfolk, "
     "PermanentTappedForSourceAbilitiesThisTurnPredicate()))` in `Triggers.dies(THIS)` | ACTIVATED + "
     "Triggers.dies(THIS) | Vodalian War Machine. The predicate reads the source's tapped-for list as it last existed "
     "(the entry's source snapshot)"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "the trigger captures the Vehicle's physical card identity before resolving so the perpetual modifier survives "
     "zone changes.",
     "the crewed Vehicle is the entry's triggering permanent, whose card as it was then gets the perpetual modifier, "
     "which survives zone changes."),
    (D + "CARD_PATTERNS_PERMANENTS_STATIC.md",
     "Triggers.leavesBattlefield(THIS) ControllerLosesGameOnLeavesEffect |",
     "Triggers.leavesBattlefield(THIS) ControllerLosesGameEffect |"),
    (D + "TRIGGER_SLOT_TARGETING.md",
     "     `LifeGainTriggerAnyTarget` and `DrawTriggerAnyTarget` are creature-only by their own oracle text\n"
     "     (\"target creature or player\") and deliberately do not use it.",
     "     `LifeGainTriggerAnyTarget`'s filterless branch offers the permanents its effect's declared target\n"
     "     admits (\"target creature or player\", `DealDamageToTargetCreatureOrPlayerEffect`: creatures only;\n"
     "     an any-target effect: creatures, planeswalkers and battles); `DrawTriggerAnyTarget` is creature-only."),
    (D + "TRIGGER_SLOT_TARGETING.md",
     "| Life-gain (`LifeGainTriggerAnyTarget`) | —                  | ✅ all players | ✅ creatures only |",
     "| Life-gain (`LifeGainTriggerAnyTarget`) | —                  | ✅ all players | ✅ the effect's declared permanents |"),
    (D + "PREDICATES_REFERENCE.md",
     "| `PermanentIsPairedPredicate` | `()` | the permanent is soulbond-paired with another creature (CR 702.95); "
     "soulbond's \"both are unpaired\" reads it on the triggering permanent through "
     "`TriggeringPermanentMatches(new PermanentNotPredicate(…))` | — |",
     "| `PermanentIsPairedPredicate` | `()` | the permanent is soulbond-paired with another creature (CR 702.95); "
     "soulbond's \"both are unpaired\" reads it on the triggering permanent through "
     "`TriggeringPermanentMatches(new PermanentNotPredicate(…))` | — |\n"
     "| `PermanentTappedForSourceAbilitiesThisTurnPredicate` | `()` | the permanent was tapped this turn to pay for an "
     "ability of the source (\"Merfolk tapped this turn to pay for its abilities\"); reads the source on the "
     "battlefield, else as it last existed (the entry's source snapshot, a \"when this dies\" ability) | Vodalian War "
     "Machine |"),
]

REMOVED = ["ControllerLosesGameOnLeavesEffect", "DestroyMerfolkTappedForSourceAbilitiesEffect", "UUID affectedPlayerId"]


def main():
    for rel, old, new in EDITS:
        path = ROOT / rel
        text = path.read_text()
        if old in text:
            text = text.replace(old, new)
            path.write_text(text)
        elif new and new not in text:
            raise SystemExit(f"{rel}: expected text not found:\n{old}")
    stale = []
    for path in list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = path.read_text()
        for name in REMOVED:
            if re.search(re.escape(name), text):
                stale.append(f"{path.relative_to(ROOT)}: {name}")
    if stale:
        raise SystemExit("still names a removed form:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
