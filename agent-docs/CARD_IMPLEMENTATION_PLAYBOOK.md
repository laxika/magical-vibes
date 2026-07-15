# CARD_IMPLEMENTATION_PLAYBOOK

Purpose: a minimal workflow for adding cards with fewer repeated lookups and lower token usage.

## Fast workflow

1. **Reprint check** — run `grep -r "class CardName " magical-vibes-card/src/` (replace `CardName` with the PascalCase class name). If the class already exists, just add a `@CardRegistration` annotation for the new set/collector-number and stop — no new code or tests needed.
2. Find a similar card in `CARD_PATTERN_INDEX.md` for reference.
3. Reuse existing effects from `EFFECTS_INDEX.md`.
4. Check `ACTIVATED_ABILITY_GUIDE.md` for constructor patterns and EffectSlot reference.
5. Add card class + `@CardRegistration`.
6. Use `target(filter).addEffect(slot, effect)` for targeting spells.
7. Write focused tests extending `BaseCardTest` (provides `harness`, `player1`, `player2`, `gs`, `gqs`, `gd`). Do NOT test Scryfall metadata.
8. Only if needed: add new effect record + `NormalEffectHandlerBean` handler (see below).

## Card class template

```java
@CardRegistration(set = "10E", collectorNumber = "000")
public class ExampleCard extends Card {
    public ExampleCard() {
        // For targeting spells: target(filter).addEffect(slot, effect)
        // For non-targeting effects: addEffect(slot, effect)
        // For multi-target: multiple target() calls, one per target
        // For activated abilities: addActivatedAbility(...)
    }
}
```

## Canonical patterns

- Targeted burn spell:
  - `addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(N))`
  - Targeting is computed from effects — no `setNeedsTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/Shock.java`

- **Power-based damage (fight, bite, Pack Hunt, Berserker, Arc-Lightning-style source damage, planeswalker power-to-loyalty):**
  - `GameQueryService` exposes three related but distinct queries. Pick the right one:
    - `getEffectivePower(gameData, creature)` — **raw signed stat.** Use for predicates, crew, X-cost, AI evaluation, display, and P/T math. Can be negative.
    - `getEffectiveCombatDamage(gameData, creature)` — **combat only.** Clamped to ≥ 0 and honors Belligerent-Brontodon / Bark-of-Doran "assign combat damage equal to toughness" static effects. Used by `CombatDamageService`.
    - `getPowerBasedDamage(gameData, creature)` — **non-combat "deals damage equal to its power" effects.** Clamped to ≥ 0 but ignores toughness-assign effects (they are combat-only). Used by `DamageSupport` / damage handlers in `normalfx`.
  - When implementing a new "deals damage equal to its power" effect, call `getPowerBasedDamage` and pass the result directly to `dealDamageAndDestroyIfLethal` / `dealDamageToPlayer` — do **not** add a manual `if (power > 0)` guard; the helper already clamps, and the damage primitives gate triggers on `damage > 0`.
  - Rationale: a single `getEffectivePower` call with a manual guard is the historical source of "stuck game" bugs (attacker with negative effective power sent a negative damage total to the engine). The three-way split makes "signed stat" vs. "damage amount" a compile-time-visible distinction.

- Multi-step spell resolution with shared target:
  - chain effects in order with repeated `addEffect(EffectSlot.SPELL, ...)`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/Condemn.java`

- Spell that targets stack entries:
  - `target(new StackEntryPredicateTargetFilter(...)).addEffect(EffectSlot.SPELL, new CounterSpellEffect())` if restricted
  - `addEffect(EffectSlot.SPELL, new CounterSpellEffect())` if targeting any spell (no filter needed)
  - Spell targeting is computed from effects — no `setNeedsSpellTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/t/Twincast.java`

- Static combat restriction on self:
  - `addEffect(EffectSlot.STATIC, new CantBlockEffect())`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SpinelessThug.java`

- Static "controller can't cast spells of specified types":
  - `addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE)))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SteelGolem.java`

- Aura with static effect:
  - Auras automatically derive targeting from `isAura()` — no `setNeedsTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/p/Pacifism.java`

- Aura with static + activated ability:
  - static enchanted effect + `addActivatedAbility(...)`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/ShimmeringWings.java`

- ETB trigger + activated ability with additional cost:
  - add ETB effect + ability containing cost-like effect first, then outcome effect
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommander.java`

- ETB target-opponent control handoff:
  - `setTargetFilter(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), ...))`
  - `addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerGainsControlOfSourceCreatureEffect())`
  - Targeting is computed from the ETB effect — no `setNeedsTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SleeperAgent.java`

- Composition before custom effect:
  - combine multiple existing effects in one ability/spell
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/o/OrcishArtillery.java`

- Upkeep sacrifice-unless-discard (any card):
  - `addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null))`
  - Pass `CardType.X` instead of `null` to restrict to a specific card type
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/r/RazormaneMasticore.java`

- Controller draw step may-trigger with targeting:
  - `addEffect(EffectSlot.DRAW_TRIGGERED, new MayEffect(new DealDamageToTargetCreatureEffect(N), "prompt"))`
  - `DRAW_TRIGGERED` fires only on the controller's draw step; use `EACH_DRAW_TRIGGERED` for all draw steps
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/r/RazormaneMasticore.java`

- Opponent draw trigger:
  - use `addEffect(EffectSlot.ON_OPPONENT_DRAWS, new DealDamageToPlayersEffect(N, DamageRecipient.TARGET_PLAYER))` when the effect should hit the player who drew
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/u/UnderworldDreams.java`

- Conditional self cast-cost reduction ("this spell costs {N} less to cast if …"):
  - wrap the single `ReduceOwnCastCostEffect` in a `ConditionalEffect`: `addEffect(EffectSlot.STATIC, new ConditionalEffect(new OpponentControlsMoreCreatures(M), new ReduceOwnCastCostEffect(new Fixed(N))))`
  - reuse an existing `Condition` (Metalcraft, ControlsPermanent, OpponentControlsMoreCreatures, CardsLeftGraveyardThisTurn, …); "for each …" reductions instead pass a counting `DynamicAmount` directly to `ReduceOwnCastCostEffect`. **Never add a per-variant `ReduceOwnCastCostIf*`/`Per*` record** — see `COST_MODIFICATION_HANDLERS.md`.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a/AvatarOfMight.java`

- Attacker blocked-only-by-flying-or-subtype:
  - prefer composed permanent predicates on attacker: `addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(new PermanentAnyOfPredicate(List.of(new PermanentHasKeywordPredicate(Keyword.FLYING), new PermanentHasSubtypePredicate(CardSubtype.X))), "creatures with flying or Xs"))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/e/ElvenRiders.java`

- Equipment with static keyword + evasion:
  - `addEffect(EffectSlot.STATIC, new CantBeBlockedEffect())` + `addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.X, GrantScope.EQUIPPED_CREATURE))` + equip ability
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/w/WhispersilkCloak.java`

- Creature land (manland) — enters tapped, taps for mana, animates:
  - `addEffect(EffectSlot.STATIC, new EntersTappedEffect())` + `addEffect(EffectSlot.ON_TAP, new AwardManaEffect(...))` + `addActivatedAbility(new ActivatedAbility(false, cost, List.of(new AnimatePermanentsEffect(power, toughness, subtypes, keywords, color)), description))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/f/FaerieConclave.java`

- Kindred Enchantment with ETB token creation + activated token ability:
  - `addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(N, tokenName, power, toughness, color, colors, subtypes))` + `addActivatedAbility(new ActivatedAbility(false, cost, List.of(new CreateTokenEffect(tokenName, power, toughness, color, colors, subtypes)), description))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/ClachanFestival.java`

- Per-blocker trigger ("becomes blocked by a creature"):
  - `addEffect(EffectSlot.ON_BECOMES_BLOCKED, effect, TriggerMode.PER_BLOCKER)` — fires once per blocking creature
  - TriggerMode is on the registration, not the effect — keeps effects pure and reusable
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/i/InfiltrationLens.java`

- Predicate-based targeting:
  - prefer `setTargetFilter(new PermanentPredicateTargetFilter(...))` over ad-hoc `TargetFilter` permutations
  - compose with `PermanentAllOfPredicate`, `PermanentAnyOfPredicate`, and atoms like `PermanentIsCreaturePredicate`, `PermanentIsTappedPredicate`, `PermanentColorInPredicate`, `PermanentHasSubtypePredicate`, `PermanentHasSupertypePredicate`

- Flashback spell (cast from graveyard for alternate cost, then exile):
  - `addCastingOption(new FlashbackCast("{cost}"))` + normal effects/targeting
  - Card is cast as a spell from the graveyard (counterable, triggers "whenever you cast"), then exiled whether it resolves or fizzles
  - Distinct from graveyard activated abilities (which put ABILITIES on stack, not spells)
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a/AncientGrudge.java`

- Alternate hand cast (non-mana alternate cost from hand):
  - `addCastingOption(new AlternateHandCast(List.of(new LifeCastingCost(N), new SacrificePermanentsCost(N, predicate))))`
  - Replaces normal mana cost; composed from `CastingCost` components (`LifeCastingCost`, `SacrificePermanentsCost`, `ManaCastingCost`, `TapUntappedPermanentsCost`)
  - Example (sacrifice + life): `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/d/DemonOfDeathsGate.java`
  - Example (mana + tap artifact): `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/z/ZahidDjinnOfTheLamp.java`

- Prowl ("Prowl {cost}" — an alternate hand cost gated on combat damage):
  - `addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{cost}")), CardSubtype.X))` — the second arg gates the alternate cost on "you dealt combat damage to a player this turn with a [subtype]" (a Changeling creature counts). For "with a Faerie or Rogue" (multiple qualifying subtypes) pass `Set.of(CardSubtype.FAERIE, CardSubtype.ROGUE)` instead. Populated in `CombatDamageService`, tracked per controller in `GameData.combatDamageToPlayerControllerSubtypesThisTurn`, checked by `CastingCostService.prowlConditionMet`.
  - Like evoke, it is a pure-mana alternate cost forced through a dedicated entry point (`GameService.playCardWithProwl` / harness `castWithProwl`); the availability gate runs in `SpellCastingService`.
  - "When this creature enters, if its prowl cost was paid, [effect]": wrap the ETB effect in `ConditionalEffect(new CastForProwlCost(), innerEffect)` on `ON_ENTER_BATTLEFIELD` (Latchkey Faerie = `DrawCardEffect`). The prowl flag is stamped on the `StackEntry`/`Permanent` at cast/resolution and gated by `EtbEffectResolver` (unwrap when paid, drop otherwise — CR 603.4).
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/k/KnowledgeExploitation.java`

- Graveyard cast ("You may cast this card from your graveyard"):
  - `addCastingOption(new GraveyardCast())` — uses the card's normal mana cost, no exile after resolution (unlike flashback)
  - Card goes to graveyard normally if it dies, allowing repeated graveyard casts
  - Can be combined with additional costs like `ExileNCardsFromGraveyardCost`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SkaabRuinator.java`

- Exile cast ("You may cast this card from exile"):
  - `addCastingOption(new ExileCast())` — uses the card's normal mana cost, card goes to graveyard normally after resolution
  - Can be combined with `GraveyardCast` for cards castable from both zones
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SqueeTheImmortal.java`

- Graveyard activated ability (pay mana from graveyard zone):
  - `addGraveyardActivatedAbility(new ActivatedAbility(false, cost, List.of(effect), description))` — activated ability usable while card is in graveyard
  - Distinct from `GRAVEYARD_UPKEEP_TRIGGERED` which is a triggered ability firing on upkeep
  - Blocked by Pithing Needle. Mana is paid on activation. Puts ACTIVATED_ABILITY on stack.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/m/MagmaPhoenix.java`

- Shuffle-into-library replacement effect ("if would be put into graveyard from anywhere, shuffle into library instead"):
  - `addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect())` — checked in `GraveyardService.addCardToGraveyard()`. Works for all zone transitions that route through graveyard: combat death, sacrifice, mill, discard, spell resolution.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/b/BlightsteelColossus.java`, `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/l/LegacyWeapon.java`

- Discard-to-battlefield replacement effect ("if opponent causes you to discard this card, put it onto the battlefield instead"):
  - `addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, new EnterBattlefieldOnDiscardEffect())` — checked in `CardChoiceHandlerService` during both self-discard-choice and revealed-hand-choice flows. Only applies when `gameData.discardCausedByOpponent` is true. Filtered out from triggered ability processing in `TriggerCollectionService`. ETB triggers still fire normally.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/o/ObstinateBaloth.java`

## Targeting checklist

- Targeting is computed automatically from effects — both for spells (`Card`) and activated abilities (`ActivatedAbility`).
- Override `targetSpec()` on your effect record to return a non-NONE `TargetSpec` — `TargetSpec.harmful(TargetCategory.CREATURE)`, `benign(PERMANENT)`, `harmful(ANY_TARGET)`, `benign(SPELL_ON_STACK)`, `benign(GRAVEYARD_CARD)`, etc. (`harmful` = damage/fight/destroy/exile/sacrifice; add a `PermanentPredicate` argument to narrow). This is the ONE targeting declaration; the deleted legacy `canTarget*` booleans derived from it. See `EFFECTS_INDEX.md` § "Effect targeting declarations" for the category table and a worked example.
- `EffectResolution.needsTarget(card)`, `EffectResolution.needsSpellTarget(card)`, `EffectResolution.computeAllowedTargets(card)` compute targeting from effects. `ActivatedAbility.isNeedsTarget()` and `ActivatedAbility.isNeedsSpellTarget()` are derived getters on the ability.
- For kicker/modal spells, use `EffectResolution.resolveEffects(effects, kicked, modeIndex)` to get the resolved effect list before computing targets.
- For non-battlefield targets on stack entries, use `Zone` (`Zone.GRAVEYARD`, `Zone.STACK`), not `TargetZone`.
- Add `setTargetFilter(...)` (on Card) or pass a `TargetFilter` to the `ActivatedAbility` constructor when target legality is restricted.
- Cross-group target uniqueness: by default, all targets across target groups must be distinct (matching the common MTG "another target" pattern). When the card's oracle text does NOT say "another" and its target filters can overlap (e.g. "target creature" + "target Merfolk"), call `setAllowSharedTargets(true)` to allow the same permanent for different target groups (CR 114.6c). Example: `RiverHeraldsBoon` allows a Merfolk as both targets; `BloodFeud` uses the default (distinct) since it says "another target creature".
- Multi-zone targeting (spell + permanent): when a spell targets both a spell on the stack and a permanent (e.g. Lost in the Mist), chain both effects (`CounterSpellEffect` + `ReturnToHandEffect.target()`). The engine stores the spell target in `targetId` (Zone.STACK) and permanent targets in `targetIds`. Uses multi-zone fizzle logic: only fizzles when ALL targets become illegal. Cast in tests via `castInstant(player, cardIndex, spellTargetId, permanentTargetId)`.
- Multi-zone targeting (graveyard + permanent): when a spell targets both a card in a graveyard and a permanent (e.g. Yawgmoth's Vile Offering), use `addEffect(SPELL, ReturnCardFromGraveyardEffect.builder().targetGraveyard(true)...)` for the graveyard target and `target(filter, 0, 1).addEffect(SPELL, ...)` for the permanent target. The engine stores the graveyard target in `targetId` (Zone.GRAVEYARD) and permanent targets in `targetIds`. Cast in tests via `castSorcery(player, cardIndex, graveyardCardId, List.of(permanentId))`.

## MayEffect lifecycle

`MayEffect(CardEffect wrapped, String prompt)` wraps an inner effect with a "you may" choice at resolution time. Understanding the lifecycle prevents unnecessary code-tracing:

### Flow for `UPKEEP_TRIGGERED` with `MayEffect`

1. **Trigger time**: `StepTriggerService` sees `MayEffect` in the upkeep effects → calls `gameData.queueMayAbility(card, controllerId, may, null, perm.getId())` which pushes a `StackEntry` with the `MayEffect` and `sourcePermanentId = perm.getId()`.
2. **Stack resolution**: `EffectResolutionService` encounters the `MayEffect` → dispatches to the `MayEffectHandler` in `normalfx` → sets `resolvingMayEffectFromStack = true` and adds a `PendingMayAbility`.
3. **Player prompt**: The system pauses, awaiting `MAY_ABILITY_CHOICE` input from the controller.
4. **Player responds**: `MayAbilityHandlerService.handleResolutionTimeMayChoice()` sets `resolvedMayAccepted = true/false`.
5. **Re-entry**: `EffectResolutionService` re-runs the same effect index. Now `resolvedMayAccepted != null`:
   - **Accepted**: unwraps to `may.wrapped()` and resolves the inner effect via its handler.
   - **Declined**: skips the effect entirely.

### Test pattern for MayEffect upkeep triggers

```java
// 1. Set up permanent on battlefield
// 2. Advance to upkeep
advanceToUpkeep(player1);
// 3. Resolve stack → MayEffect prompts
harness.passBothPriorities();
assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
// 4. Accept or decline
harness.handleMayAbilityChosen(player1, true);  // or false to decline
// 5. Inner effect now resolves (may trigger further interaction)
```

### Usage on cards

```java
.addEffect(EffectSlot.UPKEEP_TRIGGERED,
        new MayEffect(new YourInnerEffect(), "Do the thing?"))
```

For `ETB_TRIGGERED` and other slots, `MayEffect` also works — the resolution system handles it uniformly.

## When a new effect is actually required

Create a new `CardEffect` record only if both are true:
- behavior cannot be represented by existing effect composition
- behavior cannot be represented by existing target filter + existing effects

Then do all of:
- Add effect record in `magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/`
  - If it targets, override `targetSpec()` to return a non-NONE `TargetSpec` (category + `harmful` flag + optional `PermanentPredicate`; see `EFFECTS_INDEX.md`). This drives `EffectResolution.needsTarget()`/`needsSpellTarget()` computation, the cast-time type check, and the `targetsPlayer` flag in `CardViewFactory`.
- Add a handler in `magical-vibes-engine/.../service/effect/normalfx/`:
  ```java
  @Component
  @RequiredArgsConstructor
  public class YourNewEffectHandler implements NormalEffectHandlerBean {
      private final YourDomainSupport support; // inject *Support or services as needed

      @Override
      public Class<? extends CardEffect> handledEffect() {
          return YourNewEffect.class;
      }

      @Override
      public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
          var e = (YourNewEffect) effect;
          // handler body
      }
  }
  ```
  Add the `@Component` handler in `service/effect/normalfx/`. Spring auto-discovers it via `GameEngineConfig`; card tests and MCTS simulation reuse the same graph through `GameTestEngineContext` / `HeadlessSimulationContext`.
- For static/continuous effects, create a `@Component` implementing `StaticEffectHandlerBean` in `service/effect/staticfx/`. See **STATIC_EFFECT_HANDLERS.md** for naming, self vs non-self handlers, and registration details.
- For cast-cost modifiers (cost reductions/taxes), create a `@Component` implementing `CostModificationHandlerBean` in `service/cast/costmod/`. See **COST_MODIFICATION_HANDLERS.md** for the `onSpellItself` (spell-carried) vs battlefield-permanent split, scoping via `CostModificationSource`, and registration. `CastingCostService` is the single source of truth — never re-add `instanceof` cost chains in `GameBroadcastService`/`SpellCastingService`.
- Structural targeting (category + predicate) needs NO validator — the `targetSpec()` interpreter handles it. Add a `@ValidatesTarget`-annotated method under `service/validate/` (see `EFFECTS_INDEX.md` target validator map) ONLY as an escape hatch for a non-structural rule the spec cannot express (opponent-relation, controller/owner compare, chosen-source, null-target tolerance) — and still declare the structural `targetSpec()`:
  ```java
  @ValidatesTarget(YourNewEffect.class)
  public void validateYourNewEffect(TargetValidationContext ctx) { ... }
  // or with typed effect access:
  @ValidatesTarget(YourNewEffect.class)
  public void validateYourNewEffect(TargetValidationContext ctx, YourNewEffect effect) { ... }
  ```
  Use `TargetValidationService` helper methods: `requireTarget()`, `requireBattlefieldTarget()`, `requireCreature()`, `checkProtection()`, `requireTargetPlayer()`.
- If the effect fits an AI-scored family, implement the matching **capability interface** in `model/effect/` (`DamageDealingEffect`, `RemovalEffect`, `ManaProducingEffect`, `CardDrawingEffect`, `LifeGainEffect`, `TokenCreatingEffect`, `CreatureBoostEffect`, `StaticCreatureBoostEffect`, `KeywordGrantingEffect`, `ControlStealingEffect`, `CounterSpellingEffect`, `RegenerationEffect`) so the AI reads a FACT about it instead of `instanceof`-ing the concrete type. Implementations return existing record components — purely additive. See `EFFECTS_QUICK_REFERENCE.md` § "Capability / marker interfaces". `EffectDispatchRatchetTest` blocks any new concrete-effect `instanceof` outside `service/effect/**` / `service/validate/**`.
- Add test coverage for normal path + invalid/fizzle path if applicable

## When a new PermanentPredicate is required

Create a new predicate only when the targeting condition cannot be expressed by existing predicates or their compositions (`AllOf`, `AnyOf`, `Not`). Check **PREDICATES_REFERENCE.md** first.

Then do all of:

1. **Create predicate record** in `magical-vibes-domain/src/main/java/.../model/filter/`:
   ```java
   public record YourNewPredicate() implements PermanentPredicate {
   }
   ```
   Add constructor parameters only if the predicate needs static values (e.g. `(int maxPower)`). Dynamic predicates that read game state at evaluation time typically have no parameters.
   The base interfaces (`PermanentPredicate`, `CardPredicate`, `StackEntryPredicate`, `PlayerPredicate`, `TargetFilter`) are **sealed** — add your new record to the `permits` clause of the interface it implements.

2. **Add evaluation logic** in `PredicateEvaluationService.matchesPermanentPredicate()` (in `magical-vibes-engine/.../service/filter/PredicateEvaluationService.java`):
   - The switch is exhaustive over the sealed hierarchy, so after step 1 the file **fails to compile until you add a case** — a missing evaluation is a compile error, never a silent `false`
   - Add a new `case YourNewPredicate p ->` arm (search for `case PermanentPowerAtMostPredicate` to see examples)
   - Use `filterContext.gameData()`, `filterContext.sourceControllerId()`, `filterContext.sourceCardId()`, `filterContext.xValue()` as needed
   - Delegate to `GameQueryService` for engine-computed state (effective power/toughness, changeling-aware keywords, animation-aware `isCreature`)
   - Add the import at the top of the file

3. **Update agent-docs**:
   - Add the predicate to `PREDICATES_REFERENCE.md` in the appropriate section
   - Add the predicate to `ACTIVATED_ABILITY_GUIDE.md` if it's relevant to ability targeting
   - Add an oracle text mapping in `ORACLE_TEXT_EFFECT_MAP.md` if applicable
   - Add a card pattern entry in `CARD_PATTERN_INDEX.md`

## Lombok conventions

Domain model classes use Lombok `@Getter`/`@Setter`. Access fields via `getFieldName()`, not `fieldName()`. For example:
- `ActivatedAbility` → `ability.getEffects()`, `ability.getDescription()`, `ability.getManaCost()`
- `Card` → `card.getActivatedAbilities()`, `card.getEffects(slot)`, `card.getName()`
- `Permanent` → `perm.getAttachedTo()`, `perm.isTapped()`, `perm.getCard()`
- `StackEntry` → `entry.getSourcePermanentId()`, `entry.getTargetId()`, `entry.getControllerId()`

Records (effect classes, predicates, filters) use Java record accessors: `effect.powerBoost()`, `filter.predicate()`.

## Resolution handler templates

Normal stack-resolution handlers live in `service/effect/normalfx/` as `@Component` classes implementing `NormalEffectHandlerBean`. Inject `GameQueryService`, `GameBroadcastService`, and the relevant `*Support` class for the domain.

When adding a new effect that operates on the enchanted creature (for aura abilities):

```java
@Component
@RequiredArgsConstructor
public class YourEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return YourEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // 1. Find the aura permanent via sourcePermanentId
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (auraPerm == null) {
            return;
        }

        // 2. Find the enchanted creature via attachedTo
        UUID enchantedId = auraPerm.getAttachedTo();
        if (enchantedId == null) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            return;
        }

        // 3. Apply effect to enchantedCreature

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" affects ")
                .card(enchantedCreature.getCard())
                .text(".")
                .build());
    }
}
```

When adding a simple targeted effect:

```java
@Component
@RequiredArgsConstructor
public class YourTargetEffectHandler implements NormalEffectHandlerBean {
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return YourTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Apply effect to target

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" affects ")
                .card(target.getCard())
                .text(".")
                .build());
    }
}
```

## Writing a new "look at top N cards" effect

"Look at top N" effects use handlers in `normalfx/` with shared helpers in `LibraryRevealSupport`. Use this template when creating a new variant:

```java
@Component
@RequiredArgsConstructor
public class YourLookAtTopEffectHandler implements NormalEffectHandlerBean {
    private final LibraryRevealSupport libraryRevealSupport;
  // inject SessionManager, CardViewFactory, etc. as needed

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return YourLookAtTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (YourLookAtTopEffect) effect;
        // 1. Take cards from top of library (broadcastLook=true logs "looks at the top N cards")
        TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return;  // empty library — already logged
        // ... filter, present LibrarySearchParams choice, reorder remainder via libraryRevealSupport
    }
}
```

**Key helpers** (public on `LibraryRevealSupport`):
- `takeTopCardsFromLibrary(gameData, entry, count, broadcastLook)` → removes cards from top, returns `TopCardsResult(controllerId, topCards, playerName)` or `null` if empty.
- `reorderRemainingToBottom(gameData, controllerId, cards)` → if 1 card, puts directly on bottom; if >1, begins `LIBRARY_REORDER` interaction.

**LibrarySearchDestination options**: `HAND`, `BATTLEFIELD`, `BATTLEFIELD_TAPPED`, `TOP_OF_LIBRARY`, `GRAVEYARD`, `EXILE`, `CAST_WITHOUT_PAYING`, etc.

**Test flow** for the complete interaction:
```java
harness.passBothPriorities();                                    // effect resolves
assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
harness.getGameService().handleLibraryCardChosen(gd, player1, 0);  // choose first match (or -1 to decline)
assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
harness.getGameService().handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2, ...));  // order remaining
```

## "Shares a creature type" pattern

When comparing creature types between two objects (permanents, cards, or a mix), use this Changeling-aware pattern from `BoostBySharedCreatureTypeEffectHandler` (Coat of Arms):

```java
// For permanents on the battlefield:
List<CardSubtype> typesA = new ArrayList<>(permanentA.getCard().getSubtypes());
typesA.addAll(permanentA.getTransientSubtypes());
boolean aIsChangeling = permanentA.hasKeyword(Keyword.CHANGELING);

// For cards NOT on the battlefield (library, hand, graveyard):
List<CardSubtype> typesB = cardB.getSubtypes();
boolean bIsChangeling = cardB.getKeywords().contains(Keyword.CHANGELING);

// Shares a creature type?
boolean sharesType = (aIsChangeling && (bIsChangeling || !typesB.isEmpty()))
        || (bIsChangeling && !typesA.isEmpty())
        || typesA.stream().anyMatch(typesB::contains);
```

**Why three conditions**:
1. A is Changeling (has all types) → shares with anything that has types
2. B is Changeling (has all types) → shares with anything that has types
3. Normal overlap check

**Permanent vs Card**: Permanents can have `transientSubtypes` (from clone/copy/animate effects) and keyword grants, so use `permanent.hasKeyword()` and include `getTransientSubtypes()`. Cards in non-battlefield zones only have their printed subtypes/keywords.

## Transform DFC checklist

1. **Back face class** — Create `BackFaceName.java` in the appropriate `cards/{letter}/` package. No `@CardRegistration`. Add only engine logic (activated abilities, effects). Scryfall auto-loads metadata.
2. **Front face class** — Create `FrontFaceName.java` with `@CardRegistration`. In constructor:
   - Instantiate back face: `BackFaceName backFace = new BackFaceName(); backFace.setSetCode(getSetCode()); backFace.setCollectorNumber(getCollectorNumber()); setBackFaceCard(backFace);`
   - Add front face abilities/effects
   - Override `getBackFaceClassName()` returning `"BackFaceName"`
3. **Transform trigger** — Choose the right pattern:
   - Werewolf: `EACH_UPKEEP_TRIGGERED` + `ConditionalEffect(new NoSpellsCastLastTurn(), wrapped)` (front) / `ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), wrapped)` (back)
   - Life threshold: `ConditionalEffect(new ControllerLifeAtLeast(N), TransformSelfEffect())` or `ConditionalEffect(new ControllerLifeAtMost(N), MayEffect(TransformSelfEffect(), "..."))`
   - Counter threshold: `PutCounterOnSelfThenTransformIfThresholdEffect(counterType, N, optional, onTransformEffects)`
   - Creature count: `ConditionalEffect(new ControlsPermanentCount(N, PermanentIsCreaturePredicate), TransformSelfEffect())`
   - Activated ability: `ActivatedAbility(tap, null, List.of(TransformSelfEffect()), "...")` with optional subtype restriction
   - Inline conditional: chain effects in activated ability list, e.g. `[GainLifeEffect(1), ConditionalEffect(new ControllerLifeAtLeast(30), TransformSelfEffect())]`
4. **Tests** — See TEST_RECIPES.md "Transform DFC" recipe

### Transform card template

```java
@CardRegistration(set = "SET", collectorNumber = "NUM")
public class FrontFace extends Card {
    public FrontFace() {
        BackFace backFace = new BackFace();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Front face abilities here
    }

    @Override
    public String getBackFaceClassName() {
        return "BackFace";
    }
}
```

```java
public class BackFace extends Card {
    public BackFace() {
        // Back face abilities here
    }
}
```

## ConditionalEffect context reference

Which engine layers support each ConditionalEffect. Check this before using a conditional in a new context.

| ConditionalEffect | Static | Effect Resolution | Trigger Time |
|---|---|---|---|
| `ConditionalEffect(new ControllerLifeAtLeast(threshold), wrapped)` | yes | yes | - |
| `ConditionalEffect(new ControllerLifeAtMost(threshold), wrapped)` | - | yes | yes (upkeep) |
| `ConditionalEffect(new GainedLifeThisTurn(), wrapped)` | yes | yes | yes (end step) |
| `ConditionalEffect(new Metalcraft(), wrapped)` | yes | yes | yes (graveyard upkeep) |
| `ConditionalEffect(new Morbid(), wrapped)` | - | yes | yes (end step) |
| `ConditionalEffect(new CreatureDiedUnderYourControlThisTurn(), wrapped)` | - | yes | yes (end step) |
| `ConditionalEffect(new CardsLeftGraveyardThisTurn(), wrapped)` | - | yes | yes (end step) |
| `ConditionalEffect(new Kicked(), wrapped)` | - | yes | - |
| `ConditionalEffect(new NotKicked(), wrapped)` | - | yes | yes (end step) |
| `ConditionalEffect(new Raid(), wrapped)` | - | yes | yes (end step) |
| `ConditionalEffect(new Equipped(), wrapped)` | yes | yes | - |
| `ConditionalEffect(new Enchanted(), wrapped)` | yes | yes | - |
| `ConditionalEffect(new ControlsAnotherPermanent(filter), wrapped)` | yes | yes | - |
| `ConditionalEffect(new ControlsPermanent(filter), wrapped)` | yes | yes | yes (attack) |
| `EnchantedPermanentConditionalEffect` | yes | - | - |
| `ConditionalEffect(new ControlsPermanentCount(minCount, filter), wrapped)` | - | yes | yes (upkeep, end step) |
| `ConditionalEffect(new NoOtherPermanent(filter), wrapped)` | - | yes | yes (upkeep) |
| `ConditionalEffect(new NoSpellsCastLastTurn(), wrapped)` | - | yes | yes (each upkeep) |
| `ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), wrapped)` | - | yes | yes (each upkeep) |
| `ConditionalEffect(new ActivationCount(threshold, abilityIndex), wrapped)` | - | yes | - |
| `ConditionalEffect(new DidntAttack(), wrapped)` | - | yes | yes (end step) |
| `ConditionalEffect(new AttacksAlone(), wrapped)` | - | yes | yes (attack) |
| `ConditionalEffect(new MinimumAttackers(minimumAttackers), wrapped)` | - | yes | yes (attack) |
| `ConditionalEffect(new HasAttacker(predicate), wrapped)` | - | yes | yes (attack) |
| `ConditionalEffect(new GraveyardCardThreshold(threshold, filter), wrapped)` | yes | yes | - |
| `ConditionalEffect(new CardsInLibraryAtLeast(threshold), wrapped)` | - | yes | yes (upkeep) |
| `ConditionalEffect(new CardsInHandAtLeast(threshold), wrapped)` | - | yes | yes (upkeep) |
| `ConditionalEffect(new DefendingPlayerPoisoned(), wrapped)` | - | yes | - |
| `ConditionalEffect(new PermanentEnteredThisTurn(predicate, minCount), wrapped)` | - | yes | - |
| `ConditionalEffect(new ControllerTurn(), wrapped)` | yes | - | - |
| `ConditionalEffect(new NotControllerTurn(), wrapped)` | yes | - | - |
| `ConditionalEffect(new OpponentControlsPermanent(filter), wrapped)` | yes | yes | - |
| `ConditionalEffect(new AnyPlayerControlsPermanent(filter), wrapped)` | yes | - | - |
| `ConditionalEffect(new SelfHasKeyword(keyword), wrapped)` | yes | - | - |
| `ConditionalEffect(new TopCardOfLibraryColor(color), wrapped)` | yes | - | - |
| `ConditionalEffect(new BlockedByMinCreatures(minBlockers), wrapped)` | yes | - | - |
| `ConditionalEffect(new OpponentPoisoned(), wrapped)` | yes | - | - |

**Key:** "yes" = supported; "-" = not supported. If you need a conditional in a context marked "-", you must add a handler in the corresponding service (`staticfx` `StaticEffectHandlerBean`, `EffectResolutionService.evaluateCondition()`, or `StepTriggerService`).

## Quick anti-patterns

- Adding new effect records for simple two-step effects that already compose.
- Skipping target filters when oracle text requires constraints.
- Adding frontend/view changes for pure engine-only behavior that already serializes correctly.

