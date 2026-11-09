#!/usr/bin/env python3
"""Step 15, run 17z11c: the docs stop naming CombatOpponentReferencingEffect and the removed boost records.

Idempotent; fails if a doc or skill still names a removed form outside a "Gone" line. EFFECT_MERGE_PLAN.md is history
and is left.
"""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
docs = root / 'agent-docs'
THAT = ("the card declares no target for it, so it acts on \"that creature\", the creature on the other side of the "
        "block, which the collector records as the entry's non-targeting `targetId`")

SPECIFIC = {
    'TRIGGER_SLOT_TARGETING.md': [
        ("| `Triggers.becomesBlocked()` (granted combat-opponent effects) | Temporary or persistent granted effects "
         "implementing `CombatOpponentReferencingEffect` are collected once for each declared blocker and auto-reference "
         "that blocker. They are omitted when an effect makes a creature blocked without a blocker. | Declare blockers |",
         "| `Triggers.becomesBlocked()` (\"becomes blocked by a creature\") | Printed or granted abilities with "
         "`TriggerOptions.EACH_BLOCKER`, or a filter on the blocker, are collected once for each declared blocker; the "
         "entry records that blocker as its triggering permanent and non-targeting target (\"that creature\"). They are "
         "omitted when an effect makes a creature blocked without a blocker. On a block or \"becomes blocked by\" ability "
         "a permanent target the card declares (`target(...)`) is chosen; one it doesn't declare is that creature. | "
         "Declare blockers |"),
    ],
    'EFFECTS_QUICK_REFERENCE.md': [
        ("""- `CombatOpponentReferencingEffect` — marker for an effect that, when fired from an **Aura/Equipment**
  combat trigger (`Triggers.blocks()` / `Triggers.becomesBlocked() + TriggerOptions.EACH_BLOCKER`), acts on the enchanted/equipped
  creature's combat opponent. `CombatTriggerService` bakes that opponent in as the trigger's
  non-targeting `targetId` (so it can't fizzle) instead of prompting the controller for a target. Impl
  `DestroyCombatOpponentAtEndOfCombatEffect` (Venom — Basilisk-style Aura, non-Wall filter),
  `SkipNextUntapEffect(TapUntapScope.TARGET)` (Shield of the Righteous — equipped creature blocks →
  blocked attacker skips next untap)
""", """- Gone: `CombatOpponentReferencingEffect` (run 17z11c). On a block or "becomes blocked by a creature" ability, of
  the creature or of an Aura or Equipment, a permanent target the card doesn't declare is "that creature": the
  collectors record the creature on the other side of the block as the entry's triggering permanent and
  non-targeting `targetId` whatever the effects are (Venom, Shield of the Righteous)
"""),
        (re.compile(r"- `BoostSelfWhenCombatOpponentMatchesEffect\(PermanentPredicate opponentFilter.*\n"),
         "- Gone: `BoostSelfWhenCombatOpponentMatchesEffect` and `BoostSelfWhenBlockingKeywordEffect` (run 17z11c). "
         "\"Whenever this creature blocks or becomes blocked by one or more [filter] creatures, it gets +X/+Y\" is "
         "`BoostSelfEffect` (plus `GrantKeywordEffect(keyword, GrantScope.SELF)`) on `Triggers.blocks().withFilter(filter)` "
         "with `TriggerOptions.ONE_OR_MORE` and `Triggers.becomesBlocked().withFilter(filter)` (no `EACH_BLOCKER`: once if "
         "any blocker matches); \"blocks a creature with flying\" is `Triggers.blocks().withFilter(new "
         "PermanentHasKeywordPredicate(FLYING))` (Woolly Spider, Dwarven Soldier, Crimson Roc)\n"),
        (re.compile(r"- `DestroyCombatOpponentAtEndOfCombatEffect\(PermanentPredicate filter, boolean cannotBeRegenerated.*\n"),
         "- `DestroyCombatOpponentAtEndOfCombatEffect(boolean cannotBeRegenerated[, boolean putCounterOnSourceIfDestroyed])` "
         "— Basilisk-style \"blocks or becomes blocked by a [filter] creature, destroy that creature at end of combat\". Put "
         "on `Triggers.blocks().withFilter(filter)` + `Triggers.becomesBlocked().withFilter(filter)` "
         "(`TriggerOptions.EACH_BLOCKER`); the filter is judged as the creature blocks or becomes blocked (CR 509.3f), and "
         "\"a creature\" needs none (Deathgazer nonblack). Destroys at end of combat, not immediately. Also works on an "
         "**Aura/Equipment** (Venom = non-Wall filter): the card declares no target, so \"that creature\" is the enchanted "
         "or equipped creature's combat opponent. Set `putCounterOnSourceIfDestroyed=true` to give the remembered source "
         "permanent a +1/+1 counter at the next end step after a successful destruction (Infinite Authority)\n"),
    ],
    'EFFECTS_INDEX.md': [
        (re.compile(r"\| `DestroyCombatOpponentAtEndOfCombatEffect` \| `\(PermanentPredicate filter.*\n"),
         "| `DestroyCombatOpponentAtEndOfCombatEffect` | `(boolean cannotBeRegenerated[, boolean "
         "putCounterOnSourceIfDestroyed])` | Basilisk-style creature combat trigger: when this creature blocks or becomes "
         "blocked by a [filter] creature (the trigger's filter, `Triggers.blocks().withFilter(...)` / "
         "`Triggers.becomesBlocked().withFilter(...)`, judged as it blocks or becomes blocked), destroy that creature **at "
         "end of combat** (so it still deals combat damage). The card declares no target, so the collector records the "
         "other creature as the stack entry's non-targeting target; a `DelayedEndOfCombatTrigger` carrying "
         "`DestroyTargetPermanentEffect(cannotBeRegenerated)` is queued, which goes on the stack as the next end of combat "
         "step begins. When `putCounterOnSourceIfDestroyed` is true, the delayed trigger instead carries "
         "`DestroyTargetPermanentThenEffect(..., RegisterDelayedCountersOnTriggeringPermanentEffect(+1/+1), "
         "requiresDestruction)` with the remembered creature as its triggering permanent. Use Triggers.blocks() + "
         "Triggers.becomesBlocked() (`TriggerOptions.EACH_BLOCKER`). See Deathgazer and Infinite Authority |\n"),
        (re.compile(r"\| `BoostSelfWhenBlockingKeywordEffect` \|.*\n"), ""),
        (re.compile(r"\| `BoostSelfWhenCombatOpponentMatchesEffect` \|.*\n"),
         "| Gone: `BoostSelfWhenCombatOpponentMatchesEffect`, `BoostSelfWhenBlockingKeywordEffect` | | run 17z11c: "
         "`BoostSelfEffect` (and `GrantKeywordEffect(..., GrantScope.SELF)`) on a filtered `Triggers.blocks()` / "
         "`Triggers.becomesBlocked()` (Dwarven Soldier, Woolly Spider) |\n"),
    ],
    'ORACLE_TEXT_EFFECT_MAP.md': [
        ("| `DestroyCombatOpponentAtEndOfCombatEffect(filter, false)` in **both** Triggers.blocks() and "
         "Triggers.becomesBlocked() (`TriggerOptions.EACH_BLOCKER`) |",
         "| `DestroyCombatOpponentAtEndOfCombatEffect(false)` in **both** `Triggers.blocks().withFilter(filter)` and "
         "`Triggers.becomesBlocked().withFilter(filter)` (`TriggerOptions.EACH_BLOCKER`) |"),
        ("| `DestroyCombatOpponentAtEndOfCombatEffect(new PermanentToughnessAtMostPredicate(3), false, true)` in both "
         "Triggers.blocks() and Triggers.becomesBlocked() (`TriggerOptions.EACH_BLOCKER`) |",
         "| `DestroyCombatOpponentAtEndOfCombatEffect(false, true)` in both "
         "`Triggers.blocks().withFilter(new PermanentToughnessAtMostPredicate(3))` and "
         "`Triggers.becomesBlocked().withFilter(...)` (`TriggerOptions.EACH_BLOCKER`) |"),
        (re.compile(r"\| \"Whenever CARDNAME blocks or becomes blocked by one or more \[filter\] creatures, it gets \+X/\+Y "
                    r"until end of turn\" \| `BoostSelfWhenCombatOpponentMatchesEffect.*\n"),
         "| \"Whenever CARDNAME blocks or becomes blocked by one or more [filter] creatures, it gets +X/+Y until end of "
         "turn\" | `BoostSelfEffect(X, Y)` on `Triggers.blocks().withFilter(filter)` (`TriggerOptions.ONE_OR_MORE`) and "
         "`Triggers.becomesBlocked().withFilter(filter)` (plain, not `EACH_BLOCKER`: once if any blocker matches) | "
         "Triggers.blocks() + Triggers.becomesBlocked() | Dwarven Soldier (\"one or more Orcs\", +0/+2). Add "
         "`GrantKeywordEffect(keyword, GrantScope.SELF)` for \"and gains first strike\" (Crimson Roc, \"blocks a creature "
         "without flying\": `Triggers.blocks().withFilter(new PermanentNotPredicate(new "
         "PermanentHasKeywordPredicate(FLYING)))`); \"blocks a creature with flying\" is the same with "
         "`PermanentHasKeywordPredicate(FLYING)` (Woolly Spider) |\n"),
    ],
    'CARD_PATTERNS_CREATURES_TRIGGERED.md': [
        ("`DestroyCombatOpponentAtEndOfCombatEffect(filter, cannotBeRegenerated)` — \"blocks or becomes blocked by a "
         "[filter] creature, destroy that creature at end of combat\" (Basilisk-style).",
         "`DestroyCombatOpponentAtEndOfCombatEffect(cannotBeRegenerated)` with the filter on both triggers "
         "(`withFilter(filter)`) — \"blocks or becomes blocked by a [filter] creature, destroy that creature at end of "
         "combat\" (Basilisk-style)."),
        ("`DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false)` where `nonWall = "
         "PermanentNotPredicate(PermanentHasSubtypePredicate(WALL))`. The effect implements "
         "`CombatOpponentReferencingEffect`, so `CombatTriggerService` auto-targets the enchanted creature's combat "
         "opponent (aura/equipment flow) exactly as Deathgazer's creature triggers do",
         "`DestroyCombatOpponentAtEndOfCombatEffect(false)` with `withFilter(nonWall)` where `nonWall = "
         "PermanentNotPredicate(PermanentHasSubtypePredicate(WALL))`. The card declares no target for it, so "
         "`CombatTriggerService` records the enchanted creature's combat opponent as \"that creature\" exactly as "
         "Deathgazer's creature triggers do"),
        ("| On block conditional boost | `e/EzurisArchers.java` | Triggers.blocks() "
         "BoostSelfWhenBlockingKeywordEffect(FLYING, 3, 0) — conditional trigger checked at block time |",
         "| On block conditional boost | `e/EzurisArchers.java` | `Triggers.blocks().withFilter(new "
         "PermanentHasKeywordPredicate(FLYING))` `BoostSelfEffect(3, 0)` — the trigger's filter on the blocked creature, "
         "checked at block time |"),
        (re.compile(r"\| Blocks/becomes-blocked self-boost \(opponent-filtered\) \|.*\n"),
         "| Blocks/becomes-blocked self-boost (opponent-filtered) | `d/DwarvenSoldier.java` | "
         "`Triggers.blocks().withFilter(orc)` (`TriggerOptions.ONE_OR_MORE`) + `Triggers.becomesBlocked().withFilter(orc)` "
         "(plain: once if any blocker matches) `BoostSelfEffect(0, 2)` — \"blocks or becomes blocked by one or more Orcs, "
         "this creature gets +0/+2 until end of turn\". Add `GrantKeywordEffect(keyword, GrantScope.SELF)` for a granted "
         "keyword (Crimson Roc) |\n"),
    ],
}

GENERIC = [
    (re.compile(r" \((?:a |`\w+` is a )?`CombatOpponentReferencingEffect`(?:, non-targeting)?\)"),
     " (the card declares no target, so it is the non-targeting `targetId`, \"that creature\")"),
    (re.compile(r"(?:It implements|Implements|Also a|As a|A|Being a|a|Effect is a|`\w+` is a) "
                r"`CombatOpponentReferencingEffect`[^.|]*(?:\.|(?= \|))"),
     lambda m: THAT[0].upper() + THAT[1:] + ('.' if m.group(0).endswith('.') else '')),
    (re.compile(r"`CombatOpponentReferencingEffect`[^.|]*(?:\.|(?= \|))"),
     lambda m: THAT[0].upper() + THAT[1:] + ('.' if m.group(0).endswith('.') else '')),
]

ARCH_PARA = (
    "A block or \"becomes blocked by a creature\" trigger's \"that creature\" — the creature on the other side of the "
    "block — is never chosen by the effect's class: the block collectors (the blocker's own and effect-made blocks, "
    "`CombatBlockService`), the \"becomes blocked by a creature\" collector (printed, granted and continuously granted "
    "abilities with `EACH_BLOCKER` or a filter on the blocker) and the Aura and Equipment readers "
    "(`CombatTriggerService`) record it on the entry whatever the effects are: as its triggering permanent (id, "
    "controller, last-known copy) and non-targeting `targetId`, with its power and toughness as the ability triggered "
    "(an Aura's or Equipment's entry keeps the enchanted or equipped creature as its triggering permanent and the other "
    "creature as its `targetId`). A permanent or player target the card declares (`target(...)`, Elite Javelineer) is "
    "chosen as the ability goes on the stack; one it doesn't declare is that creature (CR 115.10a). A \"[filter] "
    "creature\" is the trigger's filter (`Triggers.blocks().withFilter`, `Triggers.becomesBlocked().withFilter`), judged "
    "as the creature blocks or becomes blocked (CR 509.3f): on the creature it blocks, or with `ONE_OR_MORE` any "
    "creature it blocks; for each blocker with `EACH_BLOCKER`, and without it (\"becomes blocked by one or more\") once "
    "if any blocker matches. No effect tells the collectors to do this (the `CombatOpponentReferencingEffect` "
    "interface is gone).\n\n")

arch = docs / 'ARCHITECTURE.md'
arch_text = arch.read_text()
if 'interface is gone).\n\nA spell-cast trigger' not in arch_text:
    arch_text = arch_text.replace('\nA spell-cast trigger\'s "that spell"', '\n' + ARCH_PARA + 'A spell-cast trigger\'s "that spell"', 1)
    arch.write_text(arch_text)
    print('updated ARCHITECTURE.md')

for path in sorted(docs.glob('*.md')):
    if path.name == 'EFFECT_MERGE_PLAN.md':
        continue
    text = path.read_text()
    new = text
    for old, rep in SPECIFIC.get(path.name, []):
        new = old.sub(rep, new) if isinstance(old, re.Pattern) else new.replace(old, rep)
    lines = new.split('\n')
    for i, line in enumerate(lines):
        if 'Gone' in line or path.name == 'ARCHITECTURE.md':
            continue
        line = line.replace("it carries the combat opponent through `CombatOpponentReferencingEffect` without making it "
                            "a player-chosen target",
                            "it reads the combat opponent the collector records as the entry's non-targeting target, not "
                            "a player-chosen target")
        for old, rep in GENERIC:
            line = old.sub(rep, line)
        lines[i] = line.replace('; The card declares', '; the card declares')
    new = '\n'.join(lines)
    if new != text:
        path.write_text(new)
        print('updated', path.name)

REMOVED = re.compile(r"CombatOpponentReferencingEffect|BoostSelfWhenBlockingKeywordEffect|"
                     r"BoostSelfWhenCombatOpponentMatchesEffect|DestroyCombatOpponentAtEndOfCombatEffect\((?:filter|"
                     r"PermanentPredicate|nonWall|new Permanent)")
bad = []
for path in list(docs.glob('*.md')) + list((root / '.claude/skills').rglob('*.md')):
    if path.name == 'EFFECT_MERGE_PLAN.md':
        continue
    for n, line in enumerate(path.read_text().splitlines(), 1):
        if REMOVED.search(line) and 'Gone' not in line and 'interface is gone' not in line:
            bad.append(f'{path.name}:{n}')
if bad:
    sys.exit('still names a removed form: ' + ', '.join(bad))
