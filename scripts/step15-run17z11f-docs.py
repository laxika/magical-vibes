#!/usr/bin/env python3
"""Step 15, run 17z11f: the becomes-the-target rows in agent-docs. Idempotent; fails if a doc still describes the
removed class-keyed handling."""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
DOCS = root / 'agent-docs'

RECORD = ('every becomes-the-target entry records the spell or ability that targeted as its triggering spell (as it '
          'was then, and as it last existed on the stack once it has left, CR 608.2h) and that object\'s controller as '
          'its triggering player, whatever the effects are; the effect reads "that spell or ability\'s controller" '
          'from it as the ability resolves')
COUNTER = ('an effect that acts only on a spell ("counter that spell or ability [unless …]", ward) is aimed at that '
           'object on the stack without targeting it')

EDITS = {
    'EFFECTS_INDEX.md': [
        ('- **`TriggeringSpellReferencingEffect`s** (e.g. `TargetSpellControllerLosesLifeEffect`) go onto the stack as a `TRIGGERED_ABILITY` pointed at the *triggering spell\'s* card in the `STACK` zone, so "whenever this becomes the target of a spell, that spell\'s controller loses 5 life" reuses the counterspell-companion effects with no new record (`ForsakenWastes`). Checked before the two buckets below.',
         '- **"That spell\'s controller"** (e.g. `TargetSpellControllerLosesLifeEffect`): ' + RECORD + ', so "whenever this becomes the target of a spell, that spell\'s controller loses 5 life" reuses the counterspell-companion effects with no new record (`ForsakenWastes`), and ' + COUNTER + '.'),
        ('- The same triggering-object reference path is available to `Triggers.becomesTargetOfSpellOrAbility()`; the queued ability carries the triggering spell or activated ability in the `STACK` zone and preserves the source Aura id; the permanent that became the target is the entry\'s triggering permanent. `TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect` uses both references for Fractured Loyalty.',
         '- `Triggers.becomesTargetOfSpellOrAbility()` records the same and preserves the source Aura id; the permanent that became the target is the entry\'s triggering permanent. `TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect` uses both references for Fractured Loyalty.'),
        ('Special case � "counter that spell or ability for the first time each turn" (`GlyphKeeper`): put a `CounterSpellEffect` (any `CounterSpellingEffect`) in this slot. `TriggerCollectionService` recognizes the counterspelling effect, fires the trigger **at most once per turn per permanent** (gated by `Permanent.becomeTargetCounterUsedThisTurn`, reset at turn start), and sets the trigger\'s `targetId`/`targetZone` to the triggering spell/ability\'s card in the STACK zone so the counter finds the object that targeted it. No controller restriction � it counters the controller\'s own spells too.',
         '"For the first time each turn" (`GlyphKeeper`: "counter that spell or ability") is the trigger\'s `TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN)`: the collector skips it once the permanent was targeted earlier that turn (`Permanent.becomeTargetCounterUsedThisTurn`, every targeting counts). `CounterSpellEffect` acts only on a spell, so the entry is aimed at "that spell or ability" on the stack without targeting it. No controller restriction: it counters the controller\'s own spells too.'),
        ('| `TriggeringSpellControllerSacrificesPermanentsEffect` | `(DynamicAmount count, PermanentPredicate filter)` (int-count sugar ctor) | `TriggeringSpellReferencingEffect` used by becomes-target triggers. The controller of the triggering spell or ability sacrifices matching permanents through `SacrificePermanentsEffectHandler`; it carries the triggering stack object as an internal reference rather than adding a target. Lava Runner uses one land. |',
         '| `TriggeringSpellControllerSacrificesPermanentsEffect` | `(DynamicAmount count, PermanentPredicate filter)` (int-count sugar ctor) | Used by becomes-target triggers. The controller of the entry\'s triggering spell or ability (as it is on the stack, else as it last existed there) sacrifices matching permanents through `SacrificePermanentsEffectHandler`; no target. Lava Runner uses one land. |'),
    ],
    'TRIGGER_SLOT_TARGETING.md': [
        ('For `Triggers.becomesTargetOfSpellOrAbility()`, effects implementing `TriggeringSpellReferencingEffect`\nalso carry the triggering spell or activated ability as an internal `STACK` reference. The source\npermanent id remains available so an Aura effect can re-derive its host at resolution.',
         'Every becomes-the-target entry records the spell or ability that targeted as its triggering spell and its\ncontroller as its triggering player, whatever the effects are; an entry with no other referent whose effect acts\nonly on a spell ("counter that spell or ability") is aimed at that object on the stack without targeting it. The\nsource permanent id remains available so an Aura effect can re-derive its host at resolution.'),
    ],
    'ORACLE_TEXT_EFFECT_MAP.md': [
        ('Forsaken Wastes. A `TriggeringSpellReferencingEffect` in this slot is queued against the triggering spell\'s card in the STACK zone, so the counterspell-companion `TargetSpellController*` effects work as becomes-target riders.',
         'Forsaken Wastes. The entry records the triggering spell, which the counterspell-companion `TargetSpellController*` effects read as becomes-target riders.'),
        ('| `new CounterSpellEffect()` | `Triggers.becomesTargetOfSpellOrAbility()` | Glyph Keeper. `TriggerCollectionService` special-cases any `CounterSpellingEffect` in this slot: fires at most once per turn per permanent (`Permanent.becomeTargetCounterUsedThisTurn`) and targets the triggering spell/ability\'s card in the STACK zone so it counters the object that targeted it.',
         '| `new CounterSpellEffect()` | `Triggers.becomesTargetOfSpellOrAbility()`, `TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN)` | Glyph Keeper. Skipped once the permanent was targeted earlier that turn (`Permanent.becomeTargetCounterUsedThisTurn`); the entry is aimed at "that spell or ability" on the stack without targeting it.'),
        ('`GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(), new CounterSpellEffect()), GrantScope.ALL_OWN_CREATURES)`',
         '`GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN), new CounterSpellEffect()), GrantScope.ALL_OWN_CREATURES)`'),
        ('The once-per-turn gating and targeting the triggering object are the same `CounterSpellingEffect` special case Glyph Keeper uses;',
         'The first-time-each-turn limit and "that spell or ability" are as Glyph Keeper\'s;'),
    ],
    'CARD_PATTERNS_CREATURES_TRIGGERED.md': [
        ('— a `TriggeringSpellReferencingEffect` queues the triggering spell or ability as an internal STACK reference, then deals damage (not life loss) to its controller',
         '— the entry records the triggering spell or ability, whose controller it deals damage (not life loss) to'),
        ('— a `TriggeringSpellReferencingEffect` queues the triggering spell or ability as an internal STACK reference, then reuses the forced-sacrifice choice for that object\'s controller',
         '— the entry records the triggering spell or ability, and the forced-sacrifice choice is that object\'s controller\'s'),
    ],
    'EFFECTS_QUICK_REFERENCE.md': [
        ('(a `TriggeringSpellReferencingEffect`: also usable in `Triggers.becomesTargetOfSpell()`, where it hits the triggering spell\'s controller — Forsaken Wastes)',
         '(also usable in `Triggers.becomesTargetOfSpell()`, where it hits the controller of the entry\'s triggering spell — Forsaken Wastes)'),
        ('— a `TriggeringSpellReferencingEffect` for becomes-target triggers: the controller of the triggering spell or ability sacrifices',
         '— for becomes-target triggers: the controller of the entry\'s triggering spell or ability sacrifices'),
    ],
}

for name, pairs in EDITS.items():
    path = DOCS / name
    text = path.read_text(encoding='utf-8', errors='replace')
    changed = False
    for old, new in pairs:
        if new in text:
            continue
        if text.count(old) != 1:
            sys.exit(f'{name}: expected one of {old[:80]!r}')
        text = text.replace(old, new)
        changed = True
    if changed:
        path.write_text(text, encoding='utf-8')
        print('updated', name)

stale = ['special-cases any `CounterSpellingEffect`', 'queued against the triggering spell\'s card',
         'carry the triggering spell or activated ability as an internal']
for path in DOCS.glob('*.md'):
    text = path.read_text(encoding='utf-8', errors='replace')
    for phrase in stale:
        if phrase in text:
            sys.exit(f'{path.name} still says {phrase!r}')
