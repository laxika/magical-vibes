# GAMESERVICE_EXTRACTION_PLAN

Note: `GameService` is already a thin coordinator in this codebase. The highest token sinks have moved into helper/resolution/validation services.

## Goal

Reduce context needed for card work by shrinking "read surface" per card change and making effect-target rules easier to locate.

## Current hotspots (largest token cost)

- `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/GameHelper.java`
- `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/CombatService.java`
- `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/DamageResolutionService.java`
- `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/AbilityActivationService.java`
- `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/SpellCastingService.java`
- `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/input/MayAbilityHandlerService.java`

## Phase plan

### Phase 1: Extract `GameHelper` by concern (highest ROI)

Create internal helper services and move logic in slices:
- `StateBasedActionService` (SBA checks, deaths, cleanup)
- `TriggeredAbilityQueueService` (pending triggers and sequencing)
- `AuraAttachmentService` (orphaned auras, aura control transitions)
- `LegendRuleService`

Success criteria:
- `GameHelper` reduced to orchestration facade.
- card-effect changes rarely require loading full `GameHelper`.

### Phase 2: Split `CombatService` into explicit rule units

Extract:
- `AttackLegalityService`
- `BlockLegalityService`
- `CombatDamageService`
- `CombatTriggeredEffectsService`

Success criteria:
- blocking/attack rules and combat-trigger rules can be edited independently.

### Phase 3: Split `AbilityActivationService` into validation vs execution

Extract:
- `AbilityCostValidationService`
- `ManaAbilityResolutionService`
- `ActivatedAbilityTargetingService`
- `ActivatedAbilityExecutionService`

Success criteria:
- adding/adjusting activated abilities does not require scanning a 700+ line class.

### Phase 4: Consolidate target legality paths

Current target checks are spread across:
- `StackResolutionService` fizzle checks
- `TargetValidationService`
- `MayAbilityHandlerService`

Unify into:
- `TargetLegalityService` with shared APIs for
  - cast-time validation
  - resolve-time legality/fizzle checks
  - retarget/copy legality

Success criteria:
- one canonical place for target legality decisions.

### Phase 5: Reduce `SpellCastingService` surface

Extract:
- `CastPermissionService`
- `CastCostPaymentService`
- `CastTargetSelectionService`
- `StackEntryFactory`

Success criteria:
- new casting mechanics (e.g., alternate costs) added without reopening unrelated cast logic.

## Safety rails for each phase

1. Move code with no behavior change first.
2. Keep old API signatures during migration.
3. Add characterization tests before each extraction.
4. Land each phase as small PRs to ease review and bisecting.

## Suggested order for immediate implementation

1. `GameHelper` phase 1 (largest immediate token reduction)
2. Target legality consolidation (phase 4, reduces many card bugs)
3. `CombatService` split (phase 2)
4. `AbilityActivationService` split (phase 3)
5. `SpellCastingService` split (phase 5)

