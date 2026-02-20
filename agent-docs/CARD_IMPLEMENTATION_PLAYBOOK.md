# CARD_IMPLEMENTATION_PLAYBOOK

Purpose: a minimal workflow for adding cards with fewer repeated lookups and lower token usage.

## Fast workflow

1. Identify if card is "composition-only" or requires new engine behavior.
2. Reuse existing effects from `EFFECTS_INDEX.md`.
3. Add card class + `@CardRegistration`.
4. Add/adjust target flags and target filter.
5. Write focused tests using `GameTestHarness`.
6. Only if needed: add new effect record + resolver registration.

## Card class template

```java
@CardRegistration(set = "10E", collectorNumber = "000")
public class ExampleCard extends Card {
    public ExampleCard() {
        // Optional: setNeedsTarget(true) / setNeedsSpellTarget(true)
        // Optional: setTargetFilter(...)
        // addEffect(...) and/or addActivatedAbility(...)
    }
}
```

## Canonical patterns

- Targeted burn spell:
  - `setNeedsTarget(true)`
  - `addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(N))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/Shock.java`

- Multi-step spell resolution with shared target:
  - chain effects in order with repeated `addEffect(EffectSlot.SPELL, ...)`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/Condemn.java`

- Spell that targets stack entries:
  - `setNeedsSpellTarget(true)`
  - `setTargetFilter(new SpellTypeTargetFilter(...))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/t/Twincast.java`

- Static combat restriction on self:
  - `addEffect(EffectSlot.STATIC, new CantBlockEffect())`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SpinelessThug.java`

- Aura with static effect:
  - `setNeedsTarget(true)` + static aura effect
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/p/Pacifism.java`

- Aura with static + activated ability:
  - static enchanted effect + `addActivatedAbility(...)`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/ShimmeringWings.java`

- ETB trigger + activated ability with additional cost:
  - add ETB effect + ability containing cost-like effect first, then outcome effect
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommander.java`

- ETB target-opponent control handoff:
  - `setNeedsTarget(true)` + `setTargetFilter(new OpponentPlayerTargetFilter())`
  - `addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerGainsControlOfSourceCreatureEffect())`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SleeperAgent.java`

- Composition before custom effect:
  - combine multiple existing effects in one ability/spell
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/o/OrcishArtillery.java`

## Targeting checklist

- `setNeedsTarget(true)` for permanent/player target resolution.
- `setNeedsSpellTarget(true)` for stack target resolution.
- For non-battlefield targets on stack entries, use `Zone` (`Zone.GRAVEYARD`, `Zone.STACK`), not `TargetZone`.
- Add `setTargetFilter(...)` when target legality is restricted.
- For activated abilities, use `new ActivatedAbility(..., needsTarget, ..., optionalFilter)` when per-ability targeting differs.

## When a new effect is actually required

Create a new `CardEffect` record only if both are true:
- behavior cannot be represented by existing effect composition
- behavior cannot be represented by existing target filter + existing effects

Then do all of:
- Add effect record in `magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/`
- Register handler in the correct provider (see `EFFECTS_INDEX.md`)
- Add test coverage for normal path + invalid/fizzle path if applicable

## Quick anti-patterns

- Adding new effect records for simple two-step effects that already compose.
- Skipping target filters when oracle text requires constraints.
- Adding frontend/view changes for pure engine-only behavior that already serializes correctly.

