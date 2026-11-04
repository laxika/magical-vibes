#!/usr/bin/env python3
"""Rewrites the agent-docs and skill passages that describe the combat abilities' legacy view
(effect-slot-refactor.md step 14, run 16f): the combat collectors read the ability itself through
CombatAbilityReader, BatchedCombatDamageToYouTriggerEffect is gone, and "becomes blocked by a creature" /
"blocks" once each combat are TriggerOptions.EACH_BLOCKER / ONE_OR_MORE, not the deleted TriggerMode.PER_BLOCKER /
ONCE_PER_BLOCK.

Exact replacements; a passage already rewritten is left alone, so running it again changes nothing.

Usage: python3 scripts/step14-combat-ability-docs.py
"""
import os
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")

REPLACEMENTS = {
    "agent-docs/ARCHITECTURE.md": [
        ("still run where the slots were collected, reading the abilities through `CombatWatch` (one constant per old "
         "slot; `SpellAbilityWatch` and `CombatWatch` are `TriggerWatch`es), and their handlers are keyed with "
         "`@CollectsCombatTrigger`.",
         "still run where the slots were collected, reading the abilities by `CombatWatch` (one constant per old "
         "slot; `SpellAbilityWatch` and `CombatWatch` are `TriggerWatch`es) through `CombatAbilityReader`, and their "
         "handlers are keyed with `@CollectsCombatTrigger`."),
        ("`CombatWatch` reads the legacy view (`TriggeredAbility.legacyEffects()`), which hands the collectors those as "
         "the wrappers and modes they judge: `TriggeringPermanentConditionalEffect` for the filter, every effect of \"a "
         "creature you control deals combat damage to a player [or battle]\" as an `AllyCombatDamageTriggerEffect` with "
         "the filter and \"one or more\", `BatchedCombatDamageToYouTriggerEffect`, and the registrations' "
         "`TriggerMode.ONCE_PER_BLOCK` / `PER_BLOCKER`; so do the granted, registered-global and graveyard readers "
         "(`Permanent.getLegacyTemporaryTriggerEffects` / `getLegacyPersistentTriggerEffects`, "
         "`TemporaryGlobalTriggeredAbility.legacyEffect()`, `getEffectiveGraveyardLegacyTriggerEffects`).",
         "A combat ability has no legacy view (`CombatTrigger.collectedAsAbility()`: `TriggeredAbility.legacyEffects()`, "
         "the `getLegacy…` getters and `CombatWatch.effectsOf` / `registrationsOf` throw for it): every collector reads "
         "the abilities (printed by `CombatAbilityReader.printed(card, watch)`, granted by `temporary` / `persistent` / "
         "`GrantedTriggeredAbilitySupport.grantedWatchedAbilities`, a registered one by "
         "`TemporaryGlobalTriggeredAbility.ability()`, a graveyard card's by "
         "`GameQueryService.getEffectiveGraveyardTriggeredAbilities`) and `CombatAbilityReader.read` gives each effect "
         "as a `CombatEffect` with its ability's filter on the creature (the one the watch key drops), limit (marked "
         "by `TriggeredAbility.limitKey()` on the source; \"attacks for the first time each turn\" is `ONCE_EACH_TURN`, "
         "judged by the attack count) and multiplicity, which the collector judges itself; "
         "`CombatAbilityReader.allyCombatDamage` reads the ally combat damage abilities' filter and \"one or more\" "
         "from the ability and the event bindings from an authored `AllyCombatDamageTriggerEffect`, and "
         "`CombatAbilityReader.effects(abilities, trigger)` serves a collector that reads no filter, limit or "
         "multiplicity, throwing on one. The intervening \"if\" (a `ConditionalEffect` around each effect) and the "
         "event condition (the `ConditionalEffect.atTriggerTime` inside it) still reach the bodies, which judge them "
         "in contexts of their own (the attacker count, the attacked player); code that only inspects a combat "
         "ability's effects reads `CombatAbilityReader.rawEffects`. A multiplicity no combat collector reads (\"one or "
         "more\" on an attack, \"each blocker\" off \"becomes blocked\", \"each source\" off a damage trigger) is "
         "rejected as the `TriggeredAbility` is built."),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `BatchedCombatDamageToYouTriggerEffect` | `(CardEffect wrapped)` | **Don't author** (step 14 of "
         "effect-slot-refactor.md): \"whenever one or more creatures deal combat damage to you\" is "
         "`addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE, effect)`; the legacy "
         "view gives the collector this record | Witch-King of Angmar (HOC #20) |\n", ""),
        ("Use on `Triggers.blocks()` and `Triggers.becomesBlocked()` (`PER_BLOCKER`) for Shape Stealer.",
         "Use on `Triggers.blocks()` and `Triggers.becomesBlocked()` (`TriggerOptions.EACH_BLOCKER`) for Shape "
         "Stealer."),
        ("Wrapped in `BatchedCombatDamageToYouTriggerEffect` in `Triggers.creatureDealsCombatDamageToYou()`, it "
         "instead gives control to the attacking player once per combat-damage step (Contested Game Ball).",
         "On `Triggers.creatureDealsCombatDamageToYou()` with `TriggerOptions.ONE_OR_MORE` it instead gives control "
         "to the attacking player once per combat-damage step (Contested Game Ball)."),
        ("Place in `Triggers.blocks()` and/or `Triggers.becomesBlocked()` (plain, non-PER_BLOCKER).",
         "Place in `Triggers.blocks()` and/or `Triggers.becomesBlocked()` (plain, not `EACH_BLOCKER`)."),
        ("places it in `Triggers.becomesBlocked()` (plain, non-PER_BLOCKER, so it fires once no matter how many "
         "blockers)",
         "places it in `Triggers.becomesBlocked()` (plain, not `EACH_BLOCKER`, so it fires once no matter how many "
         "blockers)"),
        ("(native non-PER_BLOCKER registrations + `getTemporaryTriggeredEffects(Triggers.becomesBlocked())`)",
         "(the native abilities that aren't `EACH_BLOCKER` + the temporarily granted ones, "
         "`CombatAbilityReader.temporary(attacker, Triggers.becomesBlocked())`)"),
        ("which fires the non-PER_BLOCKER `Triggers.becomesBlocked()` registrations, aura/equipment becomes-blocked "
         "triggers and `Triggers.creatureYouControlBecomesBlocked()`; PER_BLOCKER registrations are skipped since "
         "there is no blocker.",
         "which fires the `Triggers.becomesBlocked()` abilities that aren't `EACH_BLOCKER`, aura/equipment "
         "becomes-blocked triggers and `Triggers.creatureYouControlBecomesBlocked()`; `EACH_BLOCKER` abilities are "
         "skipped since there is no blocker."),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `BatchedCombatDamageToYouTriggerEffect(CardEffect)` — **don't author**: \"whenever one or more creatures "
         "deal combat damage to you\" is `addTrigger(Triggers.creatureDealsCombatDamageToYou(), "
         "TriggerOptions.ONE_OR_MORE, effect)`; the legacy view gives the collector this record.\n", ""),
        ("Place in Triggers.blocks() and/or Triggers.becomesBlocked() (plain, non-PER_BLOCKER).",
         "Place in Triggers.blocks() and/or Triggers.becomesBlocked() (plain, not `EACH_BLOCKER`)."),
    ],
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("in **both** Triggers.blocks() and Triggers.becomesBlocked() (plain, non-PER_BLOCKER)",
         "in **both** Triggers.blocks() and Triggers.becomesBlocked() (plain, not `EACH_BLOCKER`)"),
    ],
    "agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("Triggers.becomesBlocked() DestroySubtypeCombatOpponentEffect(VAMPIRE, true) PER_BLOCKER + equip.",
         "Triggers.becomesBlocked() TriggerOptions.EACH_BLOCKER DestroySubtypeCombatOpponentEffect(VAMPIRE, true) + "
         "equip."),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("Plain (non-PER_BLOCKER) on both slots", "Plain (not `EACH_BLOCKER`) on both triggers."),
    ],
    "agent-docs/TRIGGER_SLOT_TARGETING.md": [
        ("`CombatDamageService` routes the effect (which the legacy view hands it as the inside of an "
         "`AllyCombatDamageTriggerEffect` record carrying the trigger's filter and \"one or more\") through the same "
         "`AttackTriggerTarget` pipeline;",
         "`CombatDamageService` routes the effect (read through `CombatAbilityReader.allyCombatDamage`, which takes "
         "the trigger's filter and \"one or more\" from the ability) through the same `AttackTriggerTarget` "
         "pipeline;"),
    ],
    ".claude/skills/implement-card/SKILL.md": [
        ("`TriggeringCardConditionalEffect`, `BatchedCombatDamageToYouTriggerEffect`, `OncePerTurnTriggerEffect`, an "
         "intervening-if `ConditionalEffect` or `TriggerMode.PER_BLOCKER` / `ONCE_PER_BLOCK`,",
         "`TriggeringCardConditionalEffect`, `OncePerTurnTriggerEffect` or an intervening-if `ConditionalEffect` "
         "(\"becomes blocked by a creature\" is `TriggerOptions.EACH_BLOCKER`, \"blocks\" once each combat "
         "`TriggerOptions.ONE_OR_MORE`),"),
        ("A `TriggerMode` (`ONCE_PER_BLOCK`, `PER_BLOCKER`, `ONCE_PER_BATCH`, `INDEPENDENT`) goes after the trigger: "
         "`addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, effect)`.",
         "A `TriggerMode` (`ONCE_PER_BATCH`, `INDEPENDENT`) goes after the trigger: "
         "`addTrigger(Triggers.youAttack(), TriggerMode.INDEPENDENT, effect)`."),
    ],
    ".claude/skills/review-card/SKILL.md": [
        ("`OncePerTurnTriggerEffect` / `BatchedCombatDamageToYouTriggerEffect` / `TriggerMode.PER_BLOCKER` / "
         "`ONCE_PER_BLOCK` wrappers",
         "`OncePerTurnTriggerEffect` wrappers"),
    ],
}


def main():
    missing = []
    for relative, replacements in REPLACEMENTS.items():
        path = os.path.join(ROOT, relative)
        with open(path, encoding="utf-8") as handle:
            text = handle.read()
        changed = text
        for old, new in replacements:
            if old in changed:
                changed = changed.replace(old, new)
            elif new and new in changed:
                continue
            elif not new:
                continue
            else:
                missing.append((relative, old[:80]))
        if changed != text:
            with open(path, "w", encoding="utf-8") as handle:
                handle.write(changed)
            print("rewrote", relative)
    for relative, old in missing:
        print("not found in", relative + ":", old, file=sys.stderr)
    return 1 if missing else 0


if __name__ == "__main__":
    sys.exit(main())
