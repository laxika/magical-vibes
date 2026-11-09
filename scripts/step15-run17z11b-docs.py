#!/usr/bin/env python3
"""Step 15 run 17z11b: agent-docs no longer describe the CombatDamageTriggerContextEffect capability interface.

Idempotent; fails if a doc or skill still names a removed form outside a "Gone" line. EFFECT_MERGE_PLAN.md is history
and is left. Optional repository-root argument.
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
D = os.path.join(ROOT, 'agent-docs')
TP = ("on a damage-to-a-player trigger with no `target(...)` the collector records the player dealt damage as the "
      "entry's non-targeting `targetId`")

R = {
'ORACLE_TEXT_EFFECT_MAP.md': [
 ("the non-targeting effect uses the damaged-player trigger context and snapshots",
  "the non-targeting effect reads the player dealt damage from the entry and snapshots"),
 ("`TARGET_PLAYER` implements `CombatDamageTriggerContextEffect` (`DAMAGED_PLAYER`), so CombatDamageService bakes the damaged player as `targetId`; no `target(...)` needed",
  "no `target(...)`: " + TP),
 ("`TARGET_PLAYER` implements `CombatDamageTriggerContextEffect` (`DAMAGED_PLAYER`), so the damaged player is baked in as `targetId`; no `target(...)` needed",
  "no `target(...)`: " + TP),
 ("No `target(...)` — the effect is a `CombatDamageTriggerContextEffect` reporting `DAMAGED_PLAYER`, so `CombatDamageService` bakes the damaged player in as `targetId`;",
  "No `target(...)`: " + TP + ";"),
 ("`PutCountersOnSelfEffect` binds `SOURCE_SELF`, so the counter lands on the damage-dealing creature.",
  "The counter lands on the ability's source, the damage-dealing creature."),
 ("No `target(...)` — `DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT` bakes the damaged player in;",
  "No `target(...)`: " + TP + ";"),
 ("The effect uses `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`;",
  "\"That player\" is the entry's non-targeting `targetId`, the player dealt damage;"),
 ("No `target(...)` — `SacrificePermanentsEffect` reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER` for the `TARGET_PLAYER` recipient, so `CombatDamageService` bakes the damaged player in as `targetId`;",
  "No `target(...)`: " + TP + ";"),
 ("No `target(...)` — `CombatDamageTriggerContextEffect.DAMAGED_PLAYER` bakes the damaged player in.",
  "No `target(...)`: " + TP + "."),
 ("`CombatDamageTriggerContextEffect.DAMAGED_PLAYER` supplies the opponent who may lose.",
  "the entry's non-targeting `targetId`, the player dealt damage, is the opponent who may lose."),
 ("`CombatDamageTriggerContextEffect.DAMAGED_PLAYER` supplies the player who was dealt damage,",
  "the entry's non-targeting `targetId` is the player who was dealt damage,"),
 ("No `target(...)` — `LoseLifeEffect` reports `DAMAGED_PLAYER` trigger context for `TARGET_PLAYER`, so CombatDamageService bakes the damaged player as `targetId`;",
  "No `target(...)`: " + TP + ";"),
 ("`SacrificeSelfEffect` reports `CombatDamageTriggerContextEffect.SOURCE_SELF` and `SequenceEffect` forwards the richest step context, so the trigger's stack entry binds the source and the sacrifice resolves (a plain combat-damage entry has no source)",
  "The combat-damage entry's source is the creature, which the sacrifice reads"),
 ("No `target(...)` — `DAMAGED_PLAYER` reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`, so the damaged player is baked in as `targetId`",
  "No `target(...)`: `DAMAGED_PLAYER` reads the entry's non-targeting `targetId`, which the collector records as the player dealt damage"),
 ("The delayed effect reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`, so the damaged player controls the one-shot trigger;",
  "The player dealt damage (the entry's non-targeting `targetId`) controls the one-shot trigger;"),
],
'EFFECTS_QUICK_REFERENCE.md': [
 ("- `ReturnToHandEffect.self()` supplies `SOURCE_SELF` combat-trigger context when used in `Triggers.dealsCombatDamageToPlayer()`, so self-bounce effects retain the source permanent while remaining player-only triggers.",
  "- `ReturnToHandEffect.self()` on `Triggers.dealsCombatDamageToPlayer()` returns the ability's source, which every combat-damage entry records."),
 ("; declares `SOURCE_SELF` combat-trigger context.", "; the entry's source is the creature."),
 ("It reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER` when used on `Triggers.dealsCombatDamageToPlayer()`, so a damaged player can receive",
  "On `Triggers.dealsCombatDamageToPlayer()` the player is the entry's non-targeting `targetId`, the player dealt damage, so a damaged player can receive"),
 ("the `TARGET_PLAYER` recipient needs no `target(...)` — the effect reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`, so `CombatDamageService` bakes the damaged player in (Akki Underminer)",
  "the `TARGET_PLAYER` recipient needs no `target(...)`: " + TP + " (Akki Underminer)"),
 ("With `TARGET_PLAYER` it is also a `CombatDamageTriggerContextEffect` (`DAMAGED_PLAYER`), so on `Triggers.dealsCombatDamageToPlayer()` the damaged player is baked in as `targetId` with no `target(...)`",
  "With `TARGET_PLAYER` on `Triggers.dealsCombatDamageToPlayer()` and no `target(...)` it acts on the player dealt damage, the entry's non-targeting `targetId`"),
 ("Implements `CombatDamageTriggerContextEffect(DAMAGED_PLAYER)` so the damaged player is supplied by either damage trigger path;",
  "The damaged player is the entry's non-targeting `targetId`, which either damage trigger path records;"),
 ("and `CombatDamageTriggerContextEffect(DAMAGED_PLAYER)` binds the damaged player for combat and noncombat damage.",
  "and the damaged player is the entry's non-targeting `targetId` for combat and noncombat damage."),
 ("`TARGET_PLAYER` implements `CombatDamageTriggerContextEffect` (DAMAGED_PLAYER), so it works as an Triggers.dealsDamageToPlayer() / Triggers.dealsCombatDamageToPlayer() trigger with the damaged player baked in (Nicol Bolas)",
  "`TARGET_PLAYER` with no `target(...)` on Triggers.dealsDamageToPlayer() / Triggers.dealsCombatDamageToPlayer() acts on the player dealt damage, the entry's non-targeting `targetId` (Nicol Bolas)"),
 ("Also a `CombatDamageTriggerContextEffect` (DAMAGED_PLAYER), so it works as an Triggers.dealsDamageToPlayer() trigger (Shocker).",
  "With no `target(...)` on Triggers.dealsDamageToPlayer() it acts on the player dealt damage (Shocker)."),
 ("`CombatDamageTriggerContextEffect` (DAMAGED_PLAYER) so a combat-damage trigger needs no `target(...)` (Walker of Secret Ways)",
  "a combat-damage trigger with no `target(...)` acts on the player dealt damage (Walker of Secret Ways)"),
 ("`CombatDamageTriggerContextEffect` (DAMAGED_PLAYER) so a combat-damage trigger needs no `target(...)`, while",
  "a combat-damage trigger with no `target(...)` acts on the player dealt damage, while"),
 ("Only `TARGET_OPPONENT` returns a `CombatDamageTriggerContextEffect` context (`DAMAGED_PLAYER`).",
  "On a combat-damage trigger `TARGET_OPPONENT` is the player dealt damage, the entry's non-targeting `targetId`."),
 ("Implements the damaged-player combat trigger context (Wasitora, Nekoru Queen)",
  "On a combat-damage trigger with no `target(...)` the player is the one dealt damage (Wasitora, Nekoru Queen)"),
 ("the combat trigger context bakes that player as the non-targeting stack-entry context (Celestial Mantle)",
  "the controller of the creature the Aura enchants as it resolves, else of the creature that dealt the damage (the entry's triggering permanent) (Celestial Mantle)"),
 ("the damaged player is baked in (`CombatDamageTriggerContextEffect` → `DAMAGED_PLAYER`), no `target(...)` needed",
  "with no `target(...)` the player is the one dealt damage, the entry's non-targeting `targetId`"),
 ("implements `CombatDamageTriggerContextEffect` with `SOURCE_SELF` so the permission grant and exiled cards retain the damage-dealing source (Nashi, Moon Sage's Scion).",
  "on a combat-damage trigger the entry's source is the damage-dealing creature, which the permission grant and exiled cards keep (Nashi, Moon Sage's Scion)."),
 ("the effect binds the damaged player and source creature through combat-damage trigger context, then filters",
  "the effect reads the player dealt damage and the source creature from the entry, then filters"),
 ("SacrificeSelfEffect reports CombatDamageTriggerContextEffect.SOURCE_SELF and SequenceEffect forwards its steps' richest context, so the entry binds the source for the sacrifice (a plain combat-damage entry has no source).",
  "The combat-damage entry's source is the creature, which the sacrifice reads."),
 ("- `SacrificeSelfThenEffect(CardEffect thenEffect)` —",
  "- `SacrificeSelfThenEffect(CardEffect thenEffect)` / `SacrificeSelfThenEffect.ofTriggeringPermanent(thenEffect)` (\"you may sacrifice it\" where \"it\" is the creature that dealt the damage, Ogre-Head Helm: the entry's triggering permanent when recorded, else the source) —"),
],
'EFFECTS_INDEX.md': [
 ("`CombatDamageTriggerContextEffect.CHOSEN_PLAYER` carries that player in `targetId`.",
  "the handler reads that player from the source permanent (or its last-known information) as it resolves."),
 ("`ReturnToHandEffect.self()` supplies `SOURCE_SELF` combat-trigger context when used in `Triggers.dealsCombatDamageToPlayer()`, so self-bounce effects retain the source permanent while remaining player-only triggers.",
  "`ReturnToHandEffect.self()` on `Triggers.dealsCombatDamageToPlayer()` returns the ability's source, which every combat-damage entry records."),
 ("Uses the `DAMAGED_PLAYER` combat trigger context and a benign player target spec.",
  "With no `target(...)` on a combat-damage trigger the player is the one dealt damage; benign player target spec."),
 ("combat-damage trigger context effect: registers", "combat-damage trigger effect: registers"),
 ("Implements `CombatDamageTriggerContextEffect` → `SOURCE_SELF`, so it works on an `Triggers.dealsCombatDamageToPlayer()` slot",
  "On a `Triggers.dealsCombatDamageToPlayer()` slot the entry's source is the creature"),
 ("Implements `CombatDamageTriggerContextEffect` — `SOURCE_SELF`, so it works on an `Triggers.dealsCombatDamageToPlayer()` slot",
  "On a `Triggers.dealsCombatDamageToPlayer()` slot the entry's source is the creature"),
 ("`SequenceEffect` propagates the richest combat-damage trigger context of its steps", "one entry for the sequence"),
 ("`targetSpec()` and the combat-damage trigger context delegate to the payload (the latter floored at `SOURCE_SELF`, since the sacrifice always needs the source bound).",
  "`targetSpec()` delegates to the payload. `SacrificeSelfThenEffect.ofTriggeringPermanent(thenEffect)` sacrifices the entry's triggering permanent when recorded, else the source (Ogre-Head Helm's \"sacrifice it\")."),
 ("`SOURCE` reports `CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF` (Ammit Eternal on `Triggers.dealsCombatDamageToPlayer()`), `TARGET` reports `null`",
  "`SOURCE` acts on the entry's source (Ammit Eternal on `Triggers.dealsCombatDamageToPlayer()`), `TARGET` on a target creature"),
 ("the combat form uses `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`.",
  "the combat form acts on the player dealt damage, the entry's non-targeting `targetId`."),
 ("it carries the damaged player in `targetId` via `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`;",
  "the damaged player is the entry's non-targeting `targetId`, which the collector records;"),
 ("binds the damaged player non-targetingly through `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`",
  "reads the damaged player, the entry's non-targeting `targetId`"),
 ("binds the damaged player through `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`",
  "reads the damaged player, the entry's non-targeting `targetId`"),
 ("Also implements `CombatDamageTriggerContextEffect` → `DAMAGED_PLAYER`, so it works on an `Triggers.dealsDamageToPlayer()` / `Triggers.dealsCombatDamageToPlayer()` slot with the damaged player baked in (Shocker).",
  "With no `target(...)` on a `Triggers.dealsDamageToPlayer()` / `Triggers.dealsCombatDamageToPlayer()` slot it acts on the player dealt damage (Shocker)."),
 ("Reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER` for combat-damage triggers.",
  "On a combat-damage trigger with no `target(...)` it acts on the player dealt damage."),
 ("implements `CombatDamageTriggerContextEffect` → `DAMAGED_PLAYER`, so an `Triggers.dealsCombatDamageToPlayer()` trigger binds the damaged player with no `target(...)`",
  "an `Triggers.dealsCombatDamageToPlayer()` trigger with no `target(...)` acts on the player dealt damage"),
 ("With `TARGET_PLAYER` it implements `CombatDamageTriggerContextEffect` (`DAMAGED_PLAYER`), so as an `Triggers.dealsCombatDamageToPlayer()` trigger the damaged player is bound automatically and no `target(...)` is needed",
  "With `TARGET_PLAYER` and no `target(...)` on an `Triggers.dealsCombatDamageToPlayer()` trigger it acts on the player dealt damage"),
 ("implements the damaged-player combat trigger context.", "reads the player dealt damage, the entry's non-targeting `targetId`."),
 ("implements `CombatDamageTriggerContextEffect` — `DAMAGED_PLAYER`, so an `Triggers.dealsCombatDamageToPlayer()` trigger bakes the damaged player in with no `target(...)`.",
  "an `Triggers.dealsCombatDamageToPlayer()` trigger with no `target(...)` acts on the player dealt damage."),
 ("and `CombatDamageTriggerContextEffect` — `DAMAGED_PLAYER`, so no card-level `target(...)` is needed.",
  "; with no card-level `target(...)` it acts on the player dealt damage."),
 ("Implements `CombatDamageTriggerContextEffect`, returning `DAMAGED_PLAYER` **only** for `TARGET_OPPONENT`, so on `Triggers.dealsCombatDamageToPlayer()` the damaged player exiles and the damage-dealing creature is the tracking source.",
  "On `Triggers.dealsCombatDamageToPlayer()` `TARGET_OPPONENT` is the player dealt damage (the entry's non-targeting `targetId`) and the damage-dealing creature is the tracking source."),
 ("implements `CombatDamageTriggerContextEffect` with `ENCHANTED_CREATURE_CONTROLLER`",
  "the controller of the creature the Aura enchants as it resolves, else of the creature that dealt the damage (the entry's triggering permanent)"),
 ("(baked in as `targetId` via `CombatDamageTriggerContextEffect.DAMAGED_PLAYER`)", "(the entry's non-targeting `targetId`)"),
 ("`CombatDamageTriggerContextEffect(DAMAGED_PLAYER)` binds the damaged player and source for the handler",
  "the damaged player is the entry's non-targeting `targetId` and the source its source"),
 ("Implements `CombatDamageTriggerContextEffect(DAMAGED_PLAYER)`;", "The damaged player is the entry's non-targeting `targetId`;"),
 ("A `CombatDamageTriggerContextEffect` with `SOURCE_SELF` context.", "Acts on the entry's source, the creature."),
 ("Binds `SOURCE_SELF` in `Triggers.dealsCombatDamageToPlayer()` triggers, so", "On `Triggers.dealsCombatDamageToPlayer()` the entry's source is the creature, so"),
 ("Implements `CombatDamageTriggerContextEffect.SOURCE_SELF` so `CombatDamageService` binds the damage-dealing permanent as the source.",
  "The combat-damage entry's source is the damage-dealing permanent."),
 ("it reports `CombatDamageTriggerContextEffect.DAMAGED_PLAYER` for combat-damage triggers,",
  "on a combat-damage trigger the player is the entry's non-targeting `targetId`, the player dealt damage,"),
 ("it is a `CombatDamageTriggerContextEffect` reporting `DAMAGED_PLAYER`, so the damaged player is baked in as `targetId` with no `target(...)` on the card",
  "with no `target(...)` on the card it acts on the player dealt damage, the entry's non-targeting `targetId`"),
],
'CARD_PATTERNS_CREATURES_TRIGGERED.md': [
 ("the effect binds the damaged player and source creature through combat-damage trigger context, then filters",
  "the effect reads the player dealt damage and the source creature from the entry, then filters"),
 ("SacrificeSelfEffect reports CombatDamageTriggerContextEffect.SOURCE_SELF and SequenceEffect forwards its steps' richest context, so the entry binds the source for the sacrifice (a plain combat-damage entry has no source).",
  "The combat-damage entry's source is the creature, which the sacrifice reads."),
],
}

for name, pairs in R.items():
    p = os.path.join(D, name)
    with open(p) as f:
        src = f.read()
    for old, new in pairs:
        if old in src:
            src = src.replace(old, new)
    with open(p, 'w') as f:
        f.write(src)


EXTRA = {
'EFFECTS_INDEX.md': [
 ("into the trigger controller's hand, using the combat trigger's source permanent and snapshot. | Semblance Scanner (YNEO 9) |",
  "into the trigger controller's hand: the entry's triggering permanent as it dealt the damage (the equipped creature, which the attached collector records), else the source. | Semblance Scanner (YNEO 9) |"),
 ("| `(CardPredicate alwaysEligiblePredicate, CardPredicate mvCappedEligiblePredicate, boolean remainingToBottomRandom)` | look at top X cards (X from spell's xValue),",
  "| `(CardPredicate alwaysEligiblePredicate, CardPredicate mvCappedEligiblePredicate, boolean remainingToBottomRandom[, DynamicAmount count])` | look at top X cards (X is `count`, by default `XValue`, the spell's X; `EventValue` for a trigger's \"that many\"),"),
 ("Gishath: `(CardAllOfPredicate(CREATURE, DINOSAUR), null, true)` |",
  "Gishath: `(CardAllOfPredicate(CREATURE, DINOSAUR), null, true, new EventValue())` |"),
 ("`(DynamicAmount amount[, boolean targetPermanent])`; `ofTriggeringPermanent()` |",
  "`(DynamicAmount amount[, boolean targetPermanent])`; `ofTriggeringPermanent([DynamicAmount amount])` (Mask of the Schemer's \"it connives X\": `ofTriggeringPermanent(new EventValue())`) |"),
 ("| `SacrificeSelfThenEffect` | `(CardEffect thenEffect)` |",
  "| `SacrificeSelfThenEffect` | `(CardEffect thenEffect)` / `.ofTriggeringPermanent(thenEffect)` |"),
],
'EFFECTS_QUICK_REFERENCE.md': [
 ("conjures a fresh duplicate of the nontoken damage-dealing creature into the trigger controller's hand. Used by Semblance Scanner (YNEO 9).",
  "conjures a fresh duplicate of the nontoken damage-dealing creature (the entry's triggering permanent, else the source) into the trigger controller's hand. Used by Semblance Scanner (YNEO 9)."),
 ("deals the recorded combat damage to the source permanent's remembered as-enters player;",
  "deals the recorded combat damage to the source permanent's remembered as-enters player, read from the source (or its last-known information) as the ability resolves;"),
],
}
for name, pairs in EXTRA.items():
    p = os.path.join(D, name)
    with open(p) as f:
        src = f.read()
    for old, new in pairs:
        if new not in src and old in src:
            src = src.replace(old, new)
    with open(p, 'w') as f:
        f.write(src)

# Rows whose dash was stored as a replacement character.
p = os.path.join(D, 'EFFECTS_INDEX.md')
with open(p) as f:
    src = f.read()
src = re.sub(r"Implements `CombatDamageTriggerContextEffect` \S `SOURCE_SELF`, so it works on an "
             r"`Triggers\.dealsCombatDamageToPlayer\(\)` slot",
             "On a `Triggers.dealsCombatDamageToPlayer()` slot the entry's source is the creature", src)
src = re.sub(r"implements `CombatDamageTriggerContextEffect` \S `DAMAGED_PLAYER`, so an "
             r"`Triggers\.dealsCombatDamageToPlayer\(\)` trigger bakes the damaged player in with no `target\(\.\.\.\)`\.",
             "an `Triggers.dealsCombatDamageToPlayer()` trigger with no `target(...)` acts on the player dealt damage.", src)
src = re.sub(r"and `CombatDamageTriggerContextEffect` \S `DAMAGED_PLAYER`, so no card-level `target\(\.\.\.\)` is needed\.",
             "; with no card-level `target(...)` it acts on the player dealt damage.", src)
with open(p, 'w') as f:
    f.write(src)

# The interface's own rows.
p = os.path.join(D, 'EFFECTS_INDEX.md')
with open(p) as f:
    lines = f.read().split('\n')
lines = [l for l in lines if not l.lstrip().startswith('| `CombatDamageTriggerContextEffect` |')]
with open(p, 'w') as f:
    f.write('\n'.join(lines))
p = os.path.join(D, 'EFFECTS_QUICK_REFERENCE.md')
with open(p) as f:
    src = f.read()
RULE = ("- A damage-to-a-player trigger's \"that player\" (\"that player discards a card\", \"each creature that "
        "player controls\") is the player dealt damage, which every damage-to-a-player collector records on the "
        "entry whatever the effects are: as its triggering player and, when no target is chosen, its non-targeting "
        "`targetId`; \"that many\" is the entry's event value and \"it\" / \"that creature\" of an Aura's, "
        "Equipment's or Curse's ability its triggering permanent. A player-targeting effect the card declares no "
        "`target(...)` for acts on that player; a declared player target is chosen as the ability goes on the stack. "
        "No effect tells the collectors what to record.")
src = re.sub(r'- `CombatDamageTriggerContextEffect` — .*?(?=\n- |\n\n|\n#)', RULE, src, flags=re.S)
with open(p, 'w') as f:
    f.write(src)

p = os.path.join(D, 'ARCHITECTURE.md')
with open(p) as f:
    src = f.read()
ANCHOR = "for the resolution recheck (`TargetLegalityService`, `TargetValidationContext.triggeringPlayerId`). "
ADD = ("Every damage-to-a-player collector (the combat damage service's own, attached, curse, ally and equipped readers, "
       "the noncombat `queueNoncombatDamageToPlayerEffect`) records the event the same way whatever the effects are: the "
       "player dealt damage as the entry's triggering player and, when no target is chosen, its non-targeting "
       "`targetId`, the damage as its event value, and on an Aura's, Equipment's or Curse's ability the creature that "
       "dealt it as its triggering permanent (the ability's source stays the Aura or Equipment). A player-targeting "
       "effect the card declares no `target(...)` for is \"that player\"; the triggered modal, its modes chosen as it "
       "is put on the stack or as it resolves, and the reflexive \"when you do\" abilities "
       "(`QueueReflexiveAbilityEffect`, `RemoveCounterFromSourceThenEffect`) reuse the entry's player by the same rule. "
       "No effect tells the collectors what to record (the `CombatDamageTriggerContextEffect` interface is gone). ")
if ADD not in src:
    if ANCHOR not in src:
        sys.exit('ARCHITECTURE.md anchor not found')
    src = src.replace(ANCHOR, ANCHOR + ADD, 1)
    with open(p, 'w') as f:
        f.write(src)

bad = re.compile(r'CombatDamageTriggerContext|combatDamageTriggerContext|DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT|'
                 r'ENCHANTED_CREATURE_CONTROLLER|SOURCE_SELF|richest (step )?context|damaged-player (combat )?trigger context')
left = []
for base in (D, os.path.join(ROOT, '.claude/skills')):
    for dirpath, _, files in os.walk(base):
        for fn in files:
            if not fn.endswith('.md') or fn == 'EFFECT_MERGE_PLAN.md':
                continue
            fp = os.path.join(dirpath, fn)
            with open(fp) as f:
                for i, line in enumerate(f, 1):
                    if bad.search(line) and 'Gone' not in line and 'interface is gone' not in line:
                        left.append(f'{fp}:{i}: {line.strip()[:160]}')
if left:
    print('\n'.join(left))
    sys.exit(1)
print('docs clean')
