#!/usr/bin/env python3
"""Step 15, run 17z13e9: agent-docs follow the permanent-damage defaults.

"Whenever a creature is dealt damage, destroy it / put a counter on it" names the creature by the triggering permanent
(`DestroyReferencedPermanentEffect` / `PutCounterOnReferencedPermanentEffect` with `PermanentReference.TRIGGERING`), not
a target the default fills in; Archfiend of Spite's "a source an opponent controls" is its event condition; Taii
Wakeen's "equal to that creature's toughness" is `TriggeringPermanentToughnessEqualsEventValue`; any targeted
"is dealt damage" ability (not only a `MayEffect`) chooses its target as it goes on the stack. Fails if a doc still
describes the old forms. Idempotent. (`ARCHITECTURE.md`'s note was written by hand.)
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ('| `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()` | `Triggers.dealtDamage()`, '
         '`TriggerOptions.EACH_SOURCE` | Archfiend of Spite. "An opponent controls" is judged as the damage is dealt;',
         '| `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()` | `Triggers.dealtDamage()`, '
         '`TriggerOptions.EACH_SOURCE.withEventCondition(new NotCondition(new TargetPlayerIsController()))` | Archfiend '
         'of Spite. "An opponent controls" is the event condition, judged as the damage is dealt with the source\'s '
         'controller as "that player" (combat or not);'),
        ('| Howlpack Avenger — the trigger scans all damaged permanents, snapshots the damage amount, and uses the source '
         'creature\'s controller for the any-target choice |',
         '| Howlpack Avenger — the default records the damaged permanent you control (its controller, power and last '
         'known information) and the damage as event value and X, and the watcher\'s controller chooses the any-target '
         'as the trigger goes on the stack |'),
        ('| "whenever a creature is dealt damage, destroy it. It can\'t be regenerated" | `DestroyTargetPermanentEffect(true)` '
         '| `Triggers.creatureDealtDamage()` | Death Pits of Rath — queued entry auto-targets the damaged creature |',
         '| "whenever a creature is dealt damage, destroy it. It can\'t be regenerated" | '
         '`DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)` | `Triggers.creatureDealtDamage()` | '
         'Death Pits of Rath — "it" is the damaged creature, the entry\'s triggering permanent (no target; a targeted '
         'effect here would choose a target as the trigger goes on the stack) |\n'
         '| "whenever a creature you control is dealt damage, put a +1/+1 counter on it" | '
         '`PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE)` | '
         '`Triggers.creatureDealtDamage().withFilter(new PermanentControlledBySourceControllerPredicate())` | Rite of '
         'Passage |\n'
         '| "whenever a source you control deals noncombat damage to a creature equal to that creature\'s toughness, '
         'draw a card" | `DrawCardEffect(1)` with `TriggerOptions.DEFAULT.withEventCondition(new '
         'TriggeringPermanentToughnessEqualsEventValue())` | `Triggers.sourceYouControlDealsNoncombatDamageToCreature()` | '
         'Taii Wakeen, Perfect Shot (`OTJ` 234) — the damage (the event value) against the creature\'s toughness as it '
         'is dealt; damage already marked on it doesn\'t matter (its ruling) |'),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ('while targeted `MayEffect`s preserve the separate resolution-time may choice. The damage amount is carried as '
         'the triggered entry\'s X payload. Non-targeting effects keep the plain entry from `handleDealtDamageDefault` '
         '(damaged permanent as `targetId`, damage amount on `eventValue`)',
         'while targeted `MayEffect`s preserve the separate resolution-time may choice. The choice carries the damage as '
         'X and event value, the damage source and its controller and the damaged permanent\'s snapshot; a "target '
         'opponent or planeswalker" effect with no declared target is offered the opponents and planeswalkers. '
         'Non-targeting effects get the plain entry from `handleDealtDamageDefault` (the same record: damage on '
         '`eventValue`, the source as triggering permanent). The combat damage step uses the same default'),
        ('Any creature is dealt damage (combat or non-combat). Queued entry targets the damaged creature | Death Pits of '
         'Rath |',
         'Any creature is dealt damage (combat or non-combat). The entry records the damaged creature as triggering '
         'permanent ("it": `PermanentReference.TRIGGERING`) and non-targeting `targetId`; a targeted effect, "you may" or '
         'not, chooses its target as the trigger is put on the stack | Death Pits of Rath |'),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ('— a permanent-targeting MayEffect in this slot routes through `SpellTargetTriggerAnyTarget`,',
         '— a targeted effect on this trigger, a MayEffect or not, routes through `SpellTargetTriggerAnyTarget`,'),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.creatureDealtDamage() DestroyTargetPermanentEffect(true) \u2014 global enchantment; fires once per "
         "damaged creature (combat + noncombat), queued entry auto-targets the damaged creature and destroys it, no "
         "regeneration |",
         "Triggers.creatureDealtDamage() DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true) \u2014 "
         "global enchantment; fires once per damaged creature (combat + noncombat), \"it\" is the damaged creature, "
         "the entry's triggering permanent (no target), no regeneration |"),
        # The file's second, mis-encoded copy of the row (its dashes are mojibake): the same two changes.
        ("Triggers.creatureDealtDamage() DestroyTargetPermanentEffect(true)",
         "Triggers.creatureDealtDamage() DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true)"),
        ("queued entry auto-targets the damaged creature and destroys it",
         "\"it\" is the damaged creature, the entry's triggering permanent (no target)"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ('and is the only form the Triggers.dealtDamage() auto-targeting handles ',
         '(a triggered one with no declared `target(...)` is offered the opponents and planeswalkers by its '
         '`targetPlayerRelation()`). '),
    ],
}
STALE = [
    "queued entry auto-targets the damaged creature",
    "Queued entry targets the damaged creature",
    "is the only form the Triggers.dealtDamage() auto-targeting handles",
    "a permanent-targeting MayEffect in this slot routes",
]


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if old not in text:
                continue
            assert text.count(old) == 1, (name, old[:80])
            text = text.replace(old, new)
        path.write_text(text)
    stale = [(p.name, s) for p in DOCS.glob("*.md") for s in STALE if s in p.read_text()]
    if stale:
        sys.exit(f"stale: {stale}")
    print("docs: done")


if __name__ == "__main__":
    main()
