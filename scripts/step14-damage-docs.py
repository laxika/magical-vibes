#!/usr/bin/env python3
"""Step 14, run 8 of effect-slot-refactor.md: rewrites the agent-docs lines that author a damage ability (a Triggers
factory of scripts/step13-damage-slots.tsv) with a trigger-only wrapper, so card agents write the trigger's filter and
the ability's TriggerOptions instead, and adds "don't author" notes to the class entries of the wrappers the damage
cards no longer write (PerDamageSourceTriggerEffect, OncePerTurnPerCreatureTriggerEffect on a damage trigger).

Each edit is an exact (file, old, new) replacement that must match once; run it once.
Usage: python3 scripts/step14-damage-docs.py
"""
import sys

OEM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
EQR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
EIX = "agent-docs/EFFECTS_INDEX.md"
CPT = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
ARCH = "agent-docs/ARCHITECTURE.md"

EDITS = [
    (OEM,
     "| `TriggeringPermanentConditionalEffect(PermanentIsSourceOrPairedPredicate, DealDamageToPlayersEffect(EventValue, "
     "TARGET_PLAYER).withTriggeredTargetFilter(opponentFilter))` on `Triggers.permanentYouControlDealtDamage()` |",
     "| `DealDamageToPlayersEffect(EventValue, TARGET_PLAYER).withTriggeredTargetFilter(opponentFilter)` on "
     "`Triggers.permanentYouControlDealtDamage().withFilter(new PermanentIsSourceOrPairedPredicate())` |"),
    (OEM,
     "| `ConditionalEffect(new SourceRoomDoorUnlocked(1), new PerpetuallyBoostCreatureCardInHandEffect(2, "
     "Set.of(Keyword.HASTE)))` | `Triggers.sourceYouControlDealsNoncombatDamageToCreature()` |",
     "| `PerpetuallyBoostCreatureCardInHandEffect(2, Set.of(Keyword.HASTE))` with "
     "`TriggerOptions.DEFAULT.withInterveningIf(new SourceRoomDoorUnlocked(1))` | "
     "`Triggers.sourceYouControlDealsNoncombatDamageToCreature()` |"),
    (OEM,
     "| `TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(HERO), PutCountersOnSelfEffect("
     "PLUS_ONE_PLUS_ONE, 2))` | `Triggers.creaturesYouControlDealDamageToPlayer()` |",
     "| `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, 2)` | "
     "`Triggers.creaturesYouControlDealDamageToPlayer().withFilter(new PermanentHasSubtypePredicate(HERO))` |"),
    (OEM,
     "| `ConditionalEffect(new EventValueAtLeast(N), CreateTokenEffect.ofClueToken(1))` | `Triggers.dealtDamage()` |",
     "| `CreateTokenEffect.ofClueToken(1)` with `TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(N))` "
     "| `Triggers.dealtDamage()` |"),
    (EQR,
     "- `ConditionalEffect(new EventValueAtLeast(N), CardEffect)` — gates a trigger on its snapshotted event value "
     "being at least `N` (for example, total damage dealt by an `Triggers.dealsDamage()` trigger)",
     "- `TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(N))` — gates a trigger on its snapshotted "
     "event value being at least `N` (for example, total damage dealt by an `Triggers.dealsDamage()` trigger: "
     "\"deals 4 or more damage\")"),
    (EQR,
     "- `ConditionalEffect(new EventValueAtLeast(n), CardEffect)` — the current trigger event's recorded amount is at "
     "least `n`; use for amount-gated damage triggers such as Innocent Bystander",
     "- `TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(n))` — the current trigger event's recorded "
     "amount is at least `n`; use for amount-gated damage triggers such as Innocent Bystander"),
    (EQR,
     "(Fear of Burning Alive, optionally inside a `ConditionalEffect`)",
     "(Fear of Burning Alive, optionally with an intervening \"if\" in its `TriggerOptions`)"),
    (EQR,
     "Uses `OncePerTurnPerCreatureTriggerEffect` + `PutCounterOnReferencedPermanentEffect` (Stensia).",
     "Stensia is one ability on this and the two other damage-to-a-player triggers (`addTrigger(List.of(…), "
     "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE), "
     "PutCounterOnReferencedPermanentEffect(TRIGGERING, …))`)."),
    (CPT,
     "use `Triggers.creaturesYouControlDealDamageToOpponent()` with `TriggeringPermanentConditionalEffect("
     "PermanentHasSubtypePredicate(...), wrappedEffect)`.",
     "use `Triggers.creaturesYouControlDealDamageToOpponent().withFilter(new PermanentHasSubtypePredicate(...))` "
     "with the plain effect."),
    (CPT,
     "| `Triggers.creaturesYouControlDealDamageToPlayer()` with `TriggeringPermanentConditionalEffect("
     "PermanentHasSubtypePredicate(HERO), PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, 2))`;",
     "| `Triggers.creaturesYouControlDealDamageToPlayer().withFilter(new PermanentHasSubtypePredicate(HERO))` with "
     "`PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, 2)`;"),
    (CPT,
     "| Triggers.dealtDamage() `PerDamageSourceTriggerEffect(CreateTokenEffect)` — the marker preserves one trigger "
     "per damage source during simultaneous combat damage and is unwrapped before normal trigger dispatch;",
     "| `addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, CreateTokenEffect)` — \"whenever a source "
     "deals damage to this\" triggers once per damage source during simultaneous combat damage;"),
    (ARCH,
     "On `Triggers.dealsDamage()`, `TriggeringPermanentConditionalEffect` filters these recipients",
     "On `Triggers.dealsDamage()`, the trigger's filter (`Triggers.dealsDamage().withFilter(…)`, read as the "
     "`TriggeringPermanentConditionalEffect` of the legacy view) filters these recipients"),
]

NOTES = [
    (EIX, "| `OncePerTurnPerCreatureTriggerEffect` |",
     "Don't author it on a damage trigger: write `TriggerOptions.DEFAULT.withLimit("
     "TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE)` (step 14 of effect-slot-refactor.md). "),
]


def main():
    texts = {}
    for path, old, new in EDITS:
        text = texts.setdefault(path, open(path).read())
        if text.count(old) != 1:
            raise SystemExit("%s: expected one %r, found %d" % (path, old[:80], text.count(old)))
        texts[path] = text.replace(old, new)
    for path, row, note in NOTES:
        text = texts.setdefault(path, open(path).read())
        lines = text.split("\n")
        hits = [i for i, line in enumerate(lines) if line.startswith(row)]
        if len(hits) != 1:
            raise SystemExit("%s: expected one row %r, found %d" % (path, row, len(hits)))
        cells = lines[hits[0]].split(" | ")
        cells[2] = note + cells[2]
        lines[hits[0]] = " | ".join(cells)
        texts[path] = "\n".join(lines)
    for path, text in texts.items():
        open(path, "w").write(text)
        print(path)
    print("%d edits, %d notes" % (len(EDITS), len(NOTES)), file=sys.stderr)


if __name__ == "__main__":
    main()
