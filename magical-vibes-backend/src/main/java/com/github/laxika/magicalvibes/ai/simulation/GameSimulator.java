package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.ai.BoardEvaluator;
import com.github.laxika.magicalvibes.ai.CombatSimulator;
import com.github.laxika.magicalvibes.ai.SpellEvaluator;
import com.github.laxika.magicalvibes.config.EffectRegistryConfig;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.ability.ActivatedAbilityExecutionService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.BounceResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.CombatBlockService;
import com.github.laxika.magicalvibes.service.combat.CombatDamageService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.combat.CombatTriggerService;
import com.github.laxika.magicalvibes.service.battlefield.CopyResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.CounterResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.combat.DamageResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.DestructionResolutionService;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.ExileResolutionService;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.library.LibraryRevealResolutionService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleResolutionService;
import com.github.laxika.magicalvibes.service.library.MillResolutionService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.PreventionResolutionService;
import com.github.laxika.magicalvibes.service.ReconnectionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TargetRedirectionResolutionService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.turn.TurnResolutionService;
import com.github.laxika.magicalvibes.service.turn.AutoPassService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.effect.CardSpecificResolutionService;
import com.github.laxika.magicalvibes.service.effect.AnimationResolutionService;
import com.github.laxika.magicalvibes.service.effect.BoostResolutionService;
import com.github.laxika.magicalvibes.service.effect.CombatRestrictionResolutionService;
import com.github.laxika.magicalvibes.service.effect.KeywordGrantResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentCounterResolutionService;
import com.github.laxika.magicalvibes.service.effect.TapUntapResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EquipResolutionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.service.effect.HandlesStaticEffect;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import com.github.laxika.magicalvibes.service.effect.WinConditionResolutionService;
import com.github.laxika.magicalvibes.service.validate.BounceTargetValidators;
import com.github.laxika.magicalvibes.service.validate.CreatureModTargetValidators;
import com.github.laxika.magicalvibes.service.validate.DamageTargetValidators;
import com.github.laxika.magicalvibes.service.validate.DestructionTargetValidators;
import com.github.laxika.magicalvibes.service.validate.GraveyardTargetValidators;
import com.github.laxika.magicalvibes.service.validate.LibraryTargetValidators;
import com.github.laxika.magicalvibes.service.validate.LifeTargetValidators;
import com.github.laxika.magicalvibes.service.validate.PermanentControlTargetValidators;
import com.github.laxika.magicalvibes.service.trigger.DamageTriggerCollectorService;
import com.github.laxika.magicalvibes.service.trigger.DiscardTriggerCollectorService;
import com.github.laxika.magicalvibes.service.trigger.LandTapTriggerCollectorService;
import com.github.laxika.magicalvibes.service.trigger.MiscTriggerCollectorService;
import com.github.laxika.magicalvibes.service.trigger.SpellCastTriggerCollectorService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectorRegistry;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import com.github.laxika.magicalvibes.service.input.MayCopyHandlerService;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import com.github.laxika.magicalvibes.service.input.MayPenaltyChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.XValueChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MultiPermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceBattlefieldHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceSpellHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceTriggerHandlerService;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Provides MCTS simulation capabilities by wiring up all game services with a no-op session manager.
 * Follows the same manual wiring pattern as GameTestHarness.
 *
 * Key responsibilities:
 * - Enumerate legal actions for a given game state and player
 * - Apply actions to a (copied) game state
 * - Auto-resolve opponent decisions using heuristics
 * - Detect terminal states
 */
@Slf4j
public class GameSimulator {

    private final GameService gameService;
    private final GameQueryService gameQueryService;
    private final GameRegistry gameRegistry;
    private final BoardEvaluator boardEvaluator;
    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;
    private final CombatAttackService combatAttackService;

    public GameSimulator(GameQueryService sharedQueryService) {
        NoOpSessionManager noOpSession = new NoOpSessionManager();
        CardViewFactory cardViewFactory = new CardViewFactory();
        PermanentViewFactory permanentViewFactory = new PermanentViewFactory(cardViewFactory);
        StackEntryViewFactory stackEntryViewFactory = new StackEntryViewFactory(cardViewFactory);

        StaticEffectHandlerRegistry staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        StaticEffectResolutionService staticEffectResolutionService = new StaticEffectResolutionService(sharedQueryService, staticEffectHandlerRegistry);
        scanStaticEffectHandlers(staticEffectResolutionService, staticEffectHandlerRegistry);

        this.gameQueryService = sharedQueryService;
        PlayerInputService playerInputService = new PlayerInputService(noOpSession, cardViewFactory);
        ValidTargetService validTargetService = new ValidTargetService(gameQueryService);
        GameBroadcastService gameBroadcastService = new GameBroadcastService(
                noOpSession, cardViewFactory, permanentViewFactory, stackEntryViewFactory, gameQueryService, validTargetService);
        DraftRegistry draftRegistry = new DraftRegistry();
        this.gameRegistry = new GameRegistry();

        LegendRuleService legendRuleService = new LegendRuleService(playerInputService, gameQueryService);
        TriggeredAbilityQueueService triggeredAbilityQueueService = new TriggeredAbilityQueueService(
                gameQueryService, gameBroadcastService, playerInputService);
        CreatureControlService creatureControlService = new CreatureControlService(gameBroadcastService, gameQueryService);
        DamagePreventionService damagePreventionService = new DamagePreventionService(gameQueryService);
        GameOutcomeService gameOutcomeService = new GameOutcomeService(gameQueryService, gameBroadcastService, noOpSession, gameRegistry, draftRegistry, null);
        DeathTriggerService deathTriggerService = new DeathTriggerService(gameQueryService, gameBroadcastService);
        DrawService drawService = new DrawService(gameQueryService, gameBroadcastService, gameOutcomeService, triggeredAbilityQueueService);
        BattlefieldEntryService battlefieldEntryService = new BattlefieldEntryService(gameQueryService, gameBroadcastService, playerInputService, cardViewFactory, null, null);
        CloneService cloneService = new CloneService(gameQueryService, gameBroadcastService, playerInputService, legendRuleService, battlefieldEntryService);
        battlefieldEntryService.setCloneService(cloneService);
        WarpWorldService warpWorldService = new WarpWorldService(gameQueryService, gameBroadcastService, playerInputService, battlefieldEntryService, legendRuleService, creatureControlService, cardViewFactory, noOpSession);
        ExileService exileService = new ExileService();
        GraveyardService graveyardService = new GraveyardService(gameQueryService, gameBroadcastService, exileService, null);
        AuraAttachmentService auraAttachmentService = new AuraAttachmentService(gameQueryService, gameBroadcastService, graveyardService);
        PermanentRemovalService permanentRemovalService = new PermanentRemovalService(
                graveyardService, battlefieldEntryService, deathTriggerService, damagePreventionService, auraAttachmentService, gameQueryService, gameBroadcastService, exileService);
        TriggerCollectorRegistry triggerCollectorRegistry = new TriggerCollectorRegistry();
        MiscTriggerCollectorService miscTriggerCollectorService = new MiscTriggerCollectorService(gameBroadcastService, graveyardService, gameQueryService, exileService, drawService, null, permanentRemovalService);
        List<Object> triggerCollectorBeans = List.of(
                new SpellCastTriggerCollectorService(gameQueryService, gameBroadcastService),
                new DiscardTriggerCollectorService(gameBroadcastService, gameQueryService, damagePreventionService, permanentRemovalService),
                new LandTapTriggerCollectorService(gameQueryService, gameBroadcastService, damagePreventionService, permanentRemovalService),
                new DamageTriggerCollectorService(gameQueryService, gameBroadcastService, permanentRemovalService, creatureControlService),
                miscTriggerCollectorService
        );
        for (Object bean : triggerCollectorBeans) {
            TriggerCollectorRegistry.scanBean(bean, triggerCollectorRegistry);
        }
        TriggerCollectionService triggerCollectionService = new TriggerCollectionService(
                triggerCollectorRegistry, gameOutcomeService, playerInputService, triggeredAbilityQueueService, gameQueryService, gameBroadcastService);
        graveyardService.setTriggerCollectionService(triggerCollectionService);
        StateTriggerService stateTriggerService = new StateTriggerService(gameBroadcastService);
        StateBasedActionService stateBasedActionService = new StateBasedActionService(
                gameOutcomeService, gameQueryService, gameBroadcastService, permanentRemovalService, graveyardService, stateTriggerService);
        LifeResolutionService lifeResolutionService = new LifeResolutionService(gameQueryService, gameBroadcastService, playerInputService, triggerCollectionService);
        CombatTriggerService combatTriggerService = new CombatTriggerService(gameBroadcastService);
        this.combatAttackService = new CombatAttackService(gameQueryService, gameBroadcastService, noOpSession, triggerCollectionService, combatTriggerService);
        CombatBlockService combatBlockService = new CombatBlockService(gameQueryService, gameBroadcastService, noOpSession, combatAttackService, combatTriggerService);
        CombatDamageService combatDamageService = new CombatDamageService(gameQueryService, gameBroadcastService, gameOutcomeService, damagePreventionService, graveyardService, deathTriggerService, permanentRemovalService, playerInputService, noOpSession, triggerCollectionService, lifeResolutionService, combatAttackService, combatTriggerService);
        CombatService combatService = new CombatService(
                combatAttackService, combatBlockService, combatDamageService, gameBroadcastService, permanentRemovalService);
        TargetValidatorRegistry targetValidatorRegistry = new TargetValidatorRegistry();
        TargetValidationService targetValidationService = new TargetValidationService(gameQueryService, targetValidatorRegistry);
        List<Object> validatorBeans = List.of(
                new DamageTargetValidators(targetValidationService, gameQueryService),
                new CreatureModTargetValidators(targetValidationService),
                new DestructionTargetValidators(targetValidationService, gameQueryService),
                new GraveyardTargetValidators(targetValidationService, gameQueryService),
                new BounceTargetValidators(targetValidationService),
                new LibraryTargetValidators(targetValidationService),
                new PermanentControlTargetValidators(targetValidationService, gameQueryService),
                new LifeTargetValidators(targetValidationService)
        );
        for (Object bean : validatorBeans) {
            scanTargetValidators(bean, targetValidatorRegistry);
        }
        TargetLegalityService targetLegalityService = new TargetLegalityService(gameQueryService, targetValidationService);

        EffectHandlerRegistry effectHandlerRegistry = new EffectHandlerRegistry();
        DamageResolutionService damageResolutionService = new DamageResolutionService(graveyardService, damagePreventionService, gameOutcomeService, gameQueryService, gameBroadcastService, permanentRemovalService, triggerCollectionService, lifeResolutionService);
        ExileResolutionService exileResolutionService = new ExileResolutionService(graveyardService, gameQueryService, gameBroadcastService, permanentRemovalService, playerInputService, cardViewFactory, triggerCollectionService, battlefieldEntryService, exileService);
        PlayerInteractionResolutionService playerInteractionResolutionService = new PlayerInteractionResolutionService(drawService, graveyardService, gameQueryService, gameBroadcastService, playerInputService, noOpSession, cardViewFactory, permanentRemovalService, battlefieldEntryService, triggerCollectionService, effectHandlerRegistry);
        TurnCleanupService turnCleanupService = new TurnCleanupService(auraAttachmentService);
        DestructionResolutionService destructionResolutionService = new DestructionResolutionService(battlefieldEntryService, graveyardService, damagePreventionService, gameOutcomeService, permanentRemovalService, gameQueryService, gameBroadcastService, playerInputService, lifeResolutionService);
        PermanentControlResolutionService permanentControlResolutionService = new PermanentControlResolutionService(battlefieldEntryService, legendRuleService, gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService, triggerCollectionService, creatureControlService);
        miscTriggerCollectorService.setPermanentControlResolutionService(permanentControlResolutionService);
        LibrarySearchResolutionService librarySearchResolutionService = new LibrarySearchResolutionService(drawService, gameBroadcastService, noOpSession, cardViewFactory, gameQueryService, permanentRemovalService, playerInputService);
        GraveyardReturnResolutionService graveyardReturnResolutionService = new GraveyardReturnResolutionService(battlefieldEntryService, permanentRemovalService, legendRuleService, gameQueryService, gameBroadcastService, playerInputService, lifeResolutionService, exileService);
        PermanentCounterResolutionService permanentCounterResolutionService = new PermanentCounterResolutionService(gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService);
        List<Object> effectServices = List.of(
                damageResolutionService,
                destructionResolutionService,
                new MillResolutionService(graveyardService, gameBroadcastService, gameQueryService, permanentControlResolutionService),
                new LibraryShuffleResolutionService(gameBroadcastService, gameQueryService, permanentRemovalService),
                librarySearchResolutionService,
                new LibraryRevealResolutionService(gameQueryService, gameBroadcastService, noOpSession, cardViewFactory, battlefieldEntryService, exileService),
                new PreventionResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new CounterResolutionService(graveyardService, exileService, gameBroadcastService, gameQueryService, stateTriggerService),
                exileResolutionService,
                new CopyResolutionService(gameBroadcastService, validTargetService, gameQueryService),
                new TargetRedirectionResolutionService(gameQueryService, gameBroadcastService, playerInputService, targetLegalityService),
                graveyardReturnResolutionService,
                new BounceResolutionService(gameQueryService, gameBroadcastService, gameOutcomeService, playerInputService, permanentRemovalService),
                lifeResolutionService,
                new AnimationResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new BoostResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new KeywordGrantResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new CombatRestrictionResolutionService(gameQueryService, gameBroadcastService),
                new TapUntapResolutionService(gameQueryService, gameBroadcastService, triggerCollectionService),
                permanentCounterResolutionService,
                playerInteractionResolutionService,
                permanentControlResolutionService,
                new TurnResolutionService(combatService, gameBroadcastService, auraAttachmentService, turnCleanupService, exileService),
                new EquipResolutionService(gameQueryService, gameBroadcastService, permanentRemovalService),
                new CardSpecificResolutionService(graveyardService, warpWorldService, battlefieldEntryService, gameQueryService, gameBroadcastService, noOpSession, cardViewFactory, permanentRemovalService, legendRuleService, exileService),
                new WinConditionResolutionService(gameOutcomeService, gameBroadcastService, gameQueryService)
        );
        for (Object service : effectServices) {
            scanEffectHandlers(service, effectHandlerRegistry);
        }

        EffectResolutionService effectResolutionService = new EffectResolutionService(gameQueryService, effectHandlerRegistry, gameBroadcastService, permanentRemovalService);
        StackResolutionService stackResolutionService = new StackResolutionService(
                battlefieldEntryService, cloneService, graveyardService, legendRuleService, stateBasedActionService, gameQueryService, targetLegalityService,
                gameBroadcastService, effectResolutionService, playerInputService, triggerCollectionService, creatureControlService, stateTriggerService, exileService);
        UntapStepService untapStepService = new UntapStepService(gameQueryService, gameBroadcastService);
        StepTriggerService stepTriggerService = new StepTriggerService(drawService, gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService, battlefieldEntryService, triggerCollectionService);
        AutoPassService autoPassService = new AutoPassService(gameQueryService, gameBroadcastService, triggerCollectionService, stackResolutionService, stepTriggerService);
        TurnProgressionService turnProgressionService = new TurnProgressionService(
                combatService, gameBroadcastService, playerInputService, turnCleanupService, untapStepService, stepTriggerService, autoPassService);
        SpellCastingService spellCastingService = new SpellCastingService(
                battlefieldEntryService, gameQueryService, gameBroadcastService, turnProgressionService, targetLegalityService, permanentRemovalService, triggerCollectionService);
        ActivatedAbilityExecutionService activatedAbilityExecutionService = new ActivatedAbilityExecutionService(
                damagePreventionService, permanentRemovalService, triggerCollectionService, stateBasedActionService, gameQueryService, gameBroadcastService, playerInputService, noOpSession, lifeResolutionService);
        AbilityActivationService abilityActivationService = new AbilityActivationService(
                graveyardService, gameQueryService, gameBroadcastService, targetLegalityService, activatedAbilityExecutionService,
                playerInputService, noOpSession, permanentRemovalService, triggerCollectionService, exileService);
        ChoiceHandlerService listChoiceHandlerService = new ChoiceHandlerService(
                noOpSession, gameQueryService, warpWorldService, battlefieldEntryService, gameBroadcastService,
                playerInputService, turnProgressionService, legendRuleService);
        CardChoiceHandlerService cardChoiceHandlerService = new CardChoiceHandlerService(
                drawService, gameQueryService, graveyardService, battlefieldEntryService, gameBroadcastService,
                playerInputService, triggerCollectionService, turnProgressionService, abilityActivationService, effectResolutionService, playerInteractionResolutionService, exileService);
        InputCompletionService inputCompletionService = new InputCompletionService(
                playerInputService, gameBroadcastService, turnProgressionService, stateBasedActionService, effectResolutionService);
        PermanentChoiceTriggerHandlerService permanentChoiceTriggerHandler = new PermanentChoiceTriggerHandlerService(
                gameQueryService, gameBroadcastService, triggerCollectionService, playerInputService, turnProgressionService, effectResolutionService, inputCompletionService, battlefieldEntryService);
        PermanentChoiceSpellHandlerService permanentChoiceSpellHandler = new PermanentChoiceSpellHandlerService(
                gameQueryService, graveyardService, gameBroadcastService, triggerCollectionService, playerInputService, turnProgressionService);
        PermanentChoiceBattlefieldHandlerService permanentChoiceBattlefieldHandler = new PermanentChoiceBattlefieldHandlerService(
                inputCompletionService, gameQueryService, battlefieldEntryService, cloneService, warpWorldService, gameBroadcastService, abilityActivationService,
                permanentRemovalService, playerInputService, stateBasedActionService, triggerCollectionService, creatureControlService, turnProgressionService, effectResolutionService, damageResolutionService, destructionResolutionService, lifeResolutionService, librarySearchResolutionService);
        MultiPermanentChoiceHandlerService multiPermanentChoiceHandler = new MultiPermanentChoiceHandlerService(
                inputCompletionService, gameQueryService, gameBroadcastService, permanentRemovalService, playerInputService, stateBasedActionService, triggerCollectionService, turnProgressionService, effectResolutionService, destructionResolutionService, permanentCounterResolutionService);
        PermanentChoiceHandlerService permanentChoiceHandlerService = new PermanentChoiceHandlerService(
                permanentChoiceTriggerHandler, permanentChoiceSpellHandler, permanentChoiceBattlefieldHandler, multiPermanentChoiceHandler);
        GraveyardChoiceHandlerService graveyardChoiceHandlerService = new GraveyardChoiceHandlerService(
                gameQueryService, battlefieldEntryService, legendRuleService, gameBroadcastService, turnProgressionService, permanentRemovalService, triggerCollectionService, playerInputService, lifeResolutionService, exileService, graveyardReturnResolutionService);
        MayCastHandlerService mayCastHandlerService = new MayCastHandlerService(
                inputCompletionService, gameQueryService, graveyardService, gameBroadcastService, playerInputService, turnProgressionService, permanentRemovalService, triggerCollectionService, battlefieldEntryService, exileService);
        MayCopyHandlerService mayCopyHandlerService = new MayCopyHandlerService(
                inputCompletionService, gameQueryService, cloneService, stateBasedActionService, gameBroadcastService, playerInputService, turnProgressionService, targetLegalityService, triggerCollectionService);
        MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService = new MayPenaltyChoiceHandlerService(
                inputCompletionService, gameQueryService, graveyardService, exileService, stateTriggerService, drawService, gameBroadcastService, playerInputService, turnProgressionService, stateBasedActionService, permanentRemovalService);
        MulliganService mulliganService = new MulliganService(
                noOpSession, gameBroadcastService, turnProgressionService, battlefieldEntryService, playerInputService);
        MayMiscHandlerService mayMiscHandlerService = new MayMiscHandlerService(
                inputCompletionService, gameQueryService, drawService, gameBroadcastService, mulliganService, playerInputService, turnProgressionService, battlefieldEntryService, noOpSession);
        MayAbilityHandlerService mayAbilityHandlerService = new MayAbilityHandlerService(
                inputCompletionService, mayCastHandlerService, mayCopyHandlerService, mayPenaltyChoiceHandlerService, mayMiscHandlerService,
                gameQueryService, gameBroadcastService, playerInputService, turnProgressionService, effectResolutionService, destructionResolutionService);
        LibraryChoiceHandlerService libraryChoiceHandlerService = new LibraryChoiceHandlerService(
                noOpSession, gameQueryService, graveyardService, warpWorldService, battlefieldEntryService, legendRuleService, stateBasedActionService, gameBroadcastService,
                cardViewFactory, turnProgressionService, playerInputService, effectResolutionService, exileService);
        XValueChoiceHandlerService xValueChoiceHandlerService = new XValueChoiceHandlerService(
                gameBroadcastService, stateBasedActionService, playerInputService, turnProgressionService, effectResolutionService);
        ReconnectionService reconnectionService = new ReconnectionService(
                noOpSession, cardViewFactory, combatService, gameQueryService);

        this.gameService = new GameService(
                gameRegistry, gameQueryService, gameBroadcastService,
                combatService,
                turnProgressionService,
                listChoiceHandlerService, cardChoiceHandlerService,
                permanentChoiceHandlerService, graveyardChoiceHandlerService,
                mayAbilityHandlerService, xValueChoiceHandlerService, libraryChoiceHandlerService,
                spellCastingService,
                stackResolutionService, abilityActivationService, mulliganService, reconnectionService,
                exileResolutionService, gameOutcomeService);

        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
    }

    /**
     * Returns the list of legal actions for the given player in the current game state.
     */
    public List<SimulationAction> getLegalActions(GameData gd, UUID playerId) {
        List<SimulationAction> actions = new ArrayList<>();

        AwaitingInput awaitingInput = gd.interaction.awaitingInputType();

        if (awaitingInput == null) {
            // Normal priority — can cast spells or pass
            boolean isMainPhase = gd.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gd.currentStep == TurnStep.POSTCOMBAT_MAIN;
            boolean isActivePlayer = playerId.equals(gd.activePlayerId);

            if (isMainPhase && isActivePlayer && gd.stack.isEmpty()) {
                // Enumerate castable spells
                List<Card> hand = gd.playerHands.get(playerId);
                if (hand != null) {
                    ManaPool virtualPool = buildVirtualManaPool(gd, playerId);
                    for (int i = 0; i < hand.size(); i++) {
                        Card card = hand.get(i);
                        if (card.hasType(CardType.LAND)) continue;
                        if (card.hasType(CardType.INSTANT)) continue;
                        if (card.getManaCost() == null) continue;
                        ManaCost cost = new ManaCost(card.getManaCost());
                        if (cost.hasX()) {
                            if (!cost.canPay(virtualPool, 1)) continue;
                        } else {
                            if (!cost.canPay(virtualPool)) continue;
                        }
                        if (card.isRequiresCreatureMana() && !cost.canPayCreatureOnly(virtualPool)) {
                            continue;
                        }
                        // For targeted spells, try to find a target
                        UUID targetId = null;
                        if (card.isNeedsTarget() || card.isAura()) {
                            targetId = findBestTarget(gd, card, playerId);
                            if (targetId == null) continue; // no valid target
                        }
                        int xValue = 0;
                        if (cost.hasX()) {
                            xValue = calculateSmartX(gd, card, targetId, virtualPool);
                            if (xValue <= 0) continue;
                        }
                        actions.add(new SimulationAction.PlayCard(i, targetId, xValue));
                    }
                }
            }
            // Always can pass priority
            actions.add(new SimulationAction.PassPriority());
            return actions;
        }

        switch (awaitingInput) {
            case ATTACKER_DECLARATION -> {
                List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gd, playerId);
                List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gd, playerId, availableIndices);
                // Use CombatSimulator to find best attackers, then also offer empty/must-only attack
                List<Integer> bestAttackers = combatSimulator.findBestAttackers(gd, playerId, availableIndices, mustAttackIndices);
                if (mustAttackIndices.isEmpty()) {
                    actions.add(new SimulationAction.DeclareAttackers(List.of())); // no attack
                } else {
                    // Must-attack creatures must always be included
                    actions.add(new SimulationAction.DeclareAttackers(mustAttackIndices));
                }
                if (!bestAttackers.isEmpty() && !bestAttackers.equals(mustAttackIndices)) {
                    actions.add(new SimulationAction.DeclareAttackers(bestAttackers));
                }
                // Also try all-in attack if different from best
                if (!availableIndices.isEmpty() && !availableIndices.equals(bestAttackers)
                        && !availableIndices.equals(mustAttackIndices)) {
                    actions.add(new SimulationAction.DeclareAttackers(availableIndices));
                }
            }
            case BLOCKER_DECLARATION -> {
                // Use CombatSimulator for blockers — offer best blocking and no blocking
                actions.add(new SimulationAction.DeclareBlockers(List.of()));
                List<int[]> bestBlockers = findBestBlockerAssignments(gd, playerId);
                if (!bestBlockers.isEmpty()) {
                    actions.add(new SimulationAction.DeclareBlockers(bestBlockers));
                }
            }
            case CARD_CHOICE, DISCARD_CHOICE, REVEALED_HAND_CHOICE -> {
                var cardChoice = gd.interaction.cardChoiceContext();
                if (cardChoice != null && cardChoice.validIndices() != null) {
                    for (int idx : cardChoice.validIndices()) {
                        actions.add(new SimulationAction.ChooseCard(idx));
                    }
                }
            }
            case PERMANENT_CHOICE -> {
                var permChoice = gd.interaction.permanentChoiceContextView();
                if (permChoice != null && permChoice.validIds() != null) {
                    for (UUID id : permChoice.validIds()) {
                        actions.add(new SimulationAction.ChoosePermanent(id));
                    }
                }
            }
            case COLOR_CHOICE -> {
                var cc = gd.interaction.colorChoiceContextView();
                if (cc != null && cc.context() instanceof ChoiceContext.KeywordGrantChoice kgc) {
                    for (var kw : kgc.options()) {
                        actions.add(new SimulationAction.ChooseColor(kw.name()));
                    }
                } else {
                    actions.add(new SimulationAction.ChooseColor("WHITE"));
                    actions.add(new SimulationAction.ChooseColor("BLUE"));
                    actions.add(new SimulationAction.ChooseColor("BLACK"));
                    actions.add(new SimulationAction.ChooseColor("RED"));
                    actions.add(new SimulationAction.ChooseColor("GREEN"));
                }
            }
            case MAY_ABILITY_CHOICE -> {
                actions.add(new SimulationAction.MayAbilityChoice(true));
                actions.add(new SimulationAction.MayAbilityChoice(false));
            }
            default -> {
                // For complex interactions (library search, reorder, etc.), use heuristic
                actions.add(new SimulationAction.PassPriority());
            }
        }

        if (actions.isEmpty()) {
            actions.add(new SimulationAction.PassPriority());
        }
        return actions;
    }

    /**
     * Applies an action to the game state. The game state is mutated in place.
     * After applying, auto-resolves any pending decisions for other players.
     */
    public void applyAction(GameData gd, UUID playerId, SimulationAction action) {
        Player player = new Player(playerId, gd.playerIdToName.getOrDefault(playerId, "AI"));

        try {
            synchronized (gd) {
                switch (action) {
                    case SimulationAction.PlayCard pc -> {
                        tapLandsForCard(gd, playerId, gd.playerHands.get(playerId).get(pc.handIndex()), pc.xValue());
                        gameService.playCard(gd, player, pc.handIndex(), pc.xValue(), pc.targetId(), null);
                    }
                    case SimulationAction.PassPriority ignored ->
                            gameService.passPriority(gd, player);
                    case SimulationAction.DeclareAttackers da ->
                            gameService.declareAttackers(gd, player, da.attackerIndices(), null);
                    case SimulationAction.DeclareBlockers db -> {
                        List<BlockerAssignment> assignments = db.blockerAssignments().stream()
                                .map(a -> new BlockerAssignment(a[0], a[1]))
                                .toList();
                        gameService.declareBlockers(gd, player, assignments);
                    }
                    case SimulationAction.ChooseCard cc ->
                            gameService.handleCardChosen(gd, player, cc.cardIndex());
                    case SimulationAction.ChoosePermanent cp ->
                            gameService.handlePermanentChosen(gd, player, cp.permanentId());
                    case SimulationAction.ChooseColor col ->
                            gameService.handleListChoice(gd, player, col.color());
                    case SimulationAction.MayAbilityChoice mac ->
                            gameService.handleMayAbilityChosen(gd, player, mac.accept());
                    case SimulationAction.ActivateAbility aa ->
                            gameService.activateAbility(gd, player, findPermanentIndex(gd, playerId, aa.permanentId()),
                                    aa.abilityIndex(), 0, aa.targetId(), null);
                }
            }
        } catch (Exception e) {
            // Simulation may hit edge cases — swallow and continue
            log.trace("Simulation action failed: {}", e.getMessage());
        }

        // Auto-resolve pending decisions for any player (including opponent)
        autoResolveDecisions(gd, playerId, 10);
    }

    /**
     * Returns true if the game is over (finished or a player is at 0 or less life).
     */
    public boolean isTerminal(GameData gd) {
        if (gd.status == GameStatus.FINISHED) return true;
        for (UUID pid : gd.orderedPlayerIds) {
            if (gd.getLife(pid) <= 0) return true;
        }
        return false;
    }

    /**
     * Evaluates the game state from the AI's perspective, normalized to [0, 1].
     * 1.0 = AI wins, 0.0 = opponent wins, 0.5 = even.
     */
    public double evaluate(GameData gd, UUID aiPlayerId) {
        double raw = boardEvaluator.evaluate(gd, aiPlayerId);
        // Normalize using sigmoid-like function: map (-inf, inf) to (0, 1)
        return 1.0 / (1.0 + Math.exp(-raw / 50.0));
    }

    public GameQueryService getGameQueryService() {
        return gameQueryService;
    }

    public BoardEvaluator getBoardEvaluator() {
        return boardEvaluator;
    }

    public SpellEvaluator getSpellEvaluator() {
        return spellEvaluator;
    }

    public CombatSimulator getCombatSimulator() {
        return combatSimulator;
    }

    public GameService getGameService() {
        return gameService;
    }

    public GameRegistry getGameRegistry() {
        return gameRegistry;
    }

    // ===== Private helpers =====

    /**
     * Auto-resolves any pending interactions using heuristic decisions.
     * Loops until no more decisions are pending or max iterations reached.
     */
    private void autoResolveDecisions(GameData gd, UUID mctsPlayerId, int maxIterations) {
        for (int i = 0; i < maxIterations; i++) {
            if (isTerminal(gd)) return;

            AwaitingInput awaiting = gd.interaction.awaitingInputType();
            if (awaiting == null) {
                // Check if we need to pass priority for the non-MCTS player
                UUID priorityHolder = getPriorityPlayer(gd);
                if (priorityHolder != null && !priorityHolder.equals(mctsPlayerId)) {
                    Player oppPlayer = new Player(priorityHolder, "opp");
                    try {
                        synchronized (gd) {
                            gameService.passPriority(gd, oppPlayer);
                        }
                    } catch (Exception e) {
                        return;
                    }
                    continue;
                }
                return; // MCTS player's turn to decide
            }

            // Determine which player the interaction is for
            UUID interactionPlayer = getInteractionPlayer(gd, awaiting);
            if (interactionPlayer == null) return;

            Player resolvePlayer = new Player(interactionPlayer, gd.playerIdToName.getOrDefault(interactionPlayer, "AI"));

            try {
                synchronized (gd) {
                    resolveInteraction(gd, resolvePlayer, awaiting, mctsPlayerId);
                }
            } catch (Exception e) {
                log.trace("Auto-resolve failed: {}", e.getMessage());
                return;
            }
        }
    }

    private void resolveInteraction(GameData gd, Player player, AwaitingInput awaiting, UUID mctsPlayerId) {
        switch (awaiting) {
            case ATTACKER_DECLARATION -> {
                UUID pid = player.getId();
                List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(pid, List.of());
                List<Integer> available = new ArrayList<>();
                for (int i = 0; i < battlefield.size(); i++) {
                    Permanent perm = battlefield.get(i);
                    if (!gameQueryService.isCreature(gd, perm)) continue;
                    if (perm.isTapped()) continue;
                    if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
                    if (gameQueryService.hasKeyword(gd, perm, Keyword.DEFENDER)) continue;
                    available.add(i);
                }
                List<Integer> mustAttack = combatAttackService.getMustAttackIndices(gd, pid, available);
                List<Integer> attackers = combatSimulator.findBestAttackers(gd, pid, available, mustAttack);
                gameService.declareAttackers(gd, player, attackers, null);
            }
            case BLOCKER_DECLARATION -> {
                List<int[]> blockers = findBestBlockerAssignments(gd, player.getId());
                List<BlockerAssignment> assignments = blockers.stream()
                        .map(a -> new BlockerAssignment(a[0], a[1]))
                        .toList();
                gameService.declareBlockers(gd, player, assignments);
            }
            case CARD_CHOICE, DISCARD_CHOICE -> {
                var cc = gd.interaction.cardChoiceContext();
                if (cc != null && cc.validIndices() != null && !cc.validIndices().isEmpty()) {
                    // Pick lowest value card
                    List<Card> hand = gd.playerHands.get(player.getId());
                    int bestIdx = cc.validIndices().iterator().next();
                    if (hand != null) {
                        bestIdx = cc.validIndices().stream()
                                .min(Comparator.comparingDouble(idx ->
                                        spellEvaluator.estimateSpellValue(gd, hand.get(idx), player.getId())))
                                .orElse(bestIdx);
                    }
                    gameService.handleCardChosen(gd, player, bestIdx);
                }
            }
            case PERMANENT_CHOICE -> {
                var pc = gd.interaction.permanentChoiceContextView();
                if (pc != null && pc.validIds() != null && !pc.validIds().isEmpty()) {
                    UUID chosen = pc.validIds().iterator().next();
                    gameService.handlePermanentChosen(gd, player, chosen);
                }
            }
            case COLOR_CHOICE -> {
                var ccCtx = gd.interaction.colorChoiceContextView();
                if (ccCtx != null && ccCtx.context() instanceof ChoiceContext.KeywordGrantChoice kgc) {
                    gameService.handleListChoice(gd, player, kgc.options().getFirst().name());
                } else {
                    gameService.handleListChoice(gd, player, "RED");
                }
            }
            case MAY_ABILITY_CHOICE -> gameService.handleMayAbilityChosen(gd, player, true);
            case GRAVEYARD_CHOICE, ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE -> {
                var gc = gd.interaction.graveyardChoiceContext();
                if (gc != null && gc.validIndices() != null && !gc.validIndices().isEmpty()) {
                    gameService.handleGraveyardCardChosen(gd, player, gc.validIndices().iterator().next());
                }
            }
            case MULTI_PERMANENT_CHOICE -> {
                var mpc = gd.interaction.multiPermanentChoiceContext();
                if (mpc != null && mpc.validIds() != null && !mpc.validIds().isEmpty()) {
                    List<UUID> chosen = mpc.validIds().stream().limit(mpc.maxCount()).toList();
                    gameService.handleMultiplePermanentsChosen(gd, player, chosen);
                }
            }
            case MULTI_GRAVEYARD_CHOICE -> {
                var mgc = gd.interaction.multiGraveyardChoiceContext();
                if (mgc != null && mgc.validCardIds() != null && !mgc.validCardIds().isEmpty()) {
                    List<UUID> chosen = mgc.validCardIds().stream().limit(mgc.maxCount()).toList();
                    gameService.handleMultipleGraveyardCardsChosen(gd, player, chosen);
                }
            }
            case MULTI_ZONE_EXILE_CHOICE -> {
                var mzec = gd.interaction.multiZoneExileChoiceContext();
                if (mzec != null && mzec.validCardIds() != null && !mzec.validCardIds().isEmpty()) {
                    List<UUID> chosen = new ArrayList<>(mzec.validCardIds());
                    gameService.handleMultipleGraveyardCardsChosen(gd, player, chosen);
                }
            }
            case MIRROR_OF_FATE_CHOICE -> {
                var mfc = gd.interaction.mirrorOfFateChoiceContext();
                if (mfc != null && mfc.validCardIds() != null && !mfc.validCardIds().isEmpty()) {
                    List<UUID> chosen = mfc.validCardIds().stream().limit(mfc.maxCount()).toList();
                    gameService.handleMultipleGraveyardCardsChosen(gd, player, chosen);
                }
            }
            case COMBAT_DAMAGE_ASSIGNMENT -> {
                var cda = gd.interaction.combatDamageAssignmentContext();
                if (cda != null) {
                    Map<UUID, Integer> assignments = autoAssignCombatDamage(cda);
                    gameService.handleCombatDamageAssigned(gd, player, cda.attackerIndex(), assignments);
                }
            }
            case LIBRARY_SEARCH -> {
                var ls = gd.interaction.librarySearchContext();
                if (ls != null && ls.cards() != null && !ls.cards().isEmpty()) {
                    gameService.handleLibraryCardChosen(gd, player, 0);
                }
            }
            case SCRY -> {
                var sc = gd.interaction.scryContext();
                if (sc != null && sc.cards() != null) {
                    List<Integer> topOrder = new ArrayList<>();
                    for (int k = 0; k < sc.cards().size(); k++) topOrder.add(k);
                    gameService.handleScryCompleted(gd, player, topOrder, List.of());
                }
            }
            case LIBRARY_REORDER -> {
                var lr = gd.interaction.libraryReorderContext();
                if (lr != null && lr.cards() != null) {
                    List<Integer> order = new ArrayList<>();
                    for (int k = 0; k < lr.cards().size(); k++) order.add(k);
                    gameService.handleLibraryCardsReordered(gd, player, order);
                }
            }
            case REVEALED_HAND_CHOICE -> {
                var rhc = gd.interaction.revealedHandChoiceContext();
                if (rhc != null && rhc.validIndices() != null && !rhc.validIndices().isEmpty()) {
                    gameService.handleCardChosen(gd, player, rhc.validIndices().iterator().next());
                }
            }
            case HAND_TOP_BOTTOM_CHOICE -> gameService.handleHandTopBottomChosen(gd, player, 0, 1);
            case LIBRARY_REVEAL_CHOICE -> {
                var lrc = gd.interaction.libraryRevealChoiceContext();
                if (lrc != null && lrc.validCardIds() != null && !lrc.validCardIds().isEmpty()) {
                    gameService.handleLibraryCardChosen(gd, player, 0);
                }
            }
            default -> {
                // Unknown interaction — skip
            }
        }
    }

    private UUID getInteractionPlayer(GameData gd, AwaitingInput awaiting) {
        var ctx = gd.interaction.currentContext();
        if (ctx == null) return null;
        return switch (ctx) {
            case InteractionContext.AttackerDeclaration ad -> ad.activePlayerId();
            case InteractionContext.BlockerDeclaration bd -> bd.defenderId();
            case InteractionContext.CardChoice cc -> cc.playerId();
            case InteractionContext.PermanentChoice pc -> pc.playerId();
            case InteractionContext.GraveyardChoice gc -> gc.playerId();
            case InteractionContext.ColorChoice cc -> cc.playerId();
            case InteractionContext.MayAbilityChoice mc -> mc.playerId();
            case InteractionContext.MultiPermanentChoice mpc -> mpc.playerId();
            case InteractionContext.MultiGraveyardChoice mgc -> mgc.playerId();
            case InteractionContext.LibraryReorder lr -> lr.playerId();
            case InteractionContext.LibrarySearch ls -> ls.playerId();
            case InteractionContext.LibraryRevealChoice lrc -> lrc.playerId();
            case InteractionContext.HandTopBottomChoice htbc -> htbc.playerId();
            case InteractionContext.RevealedHandChoice rhc -> rhc.choosingPlayerId();
            case InteractionContext.MultiZoneExileChoice mzec -> mzec.playerId();
            case InteractionContext.CombatDamageAssignment cda -> cda.playerId();
            case InteractionContext.XValueChoice xvc -> xvc.playerId();
            case InteractionContext.Scry sc -> sc.playerId();
            case InteractionContext.KnowledgePoolCastChoice kpc -> kpc.playerId();
            case InteractionContext.MirrorOfFateChoice mfc -> mfc.playerId();
        };
    }

    private UUID getPriorityPlayer(GameData gd) {
        if (gd.activePlayerId == null) return null;
        if (!gd.priorityPassedBy.contains(gd.activePlayerId)) {
            return gd.activePlayerId;
        }
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(gd.activePlayerId) && !gd.priorityPassedBy.contains(id)) {
                return id;
            }
        }
        return null;
    }

    private List<int[]> findBestBlockerAssignments(GameData gd, UUID playerId) {
        UUID opponentId = getOpponentId(gd, playerId);
        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < oppBattlefield.size(); i++) {
            if (oppBattlefield.get(i).isAttacking()) attackerIndices.add(i);
        }
        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (gameQueryService.canBlock(gd, battlefield.get(i))) {
                blockerIndices.add(i);
            }
        }
        return combatSimulator.findBestBlockers(gd, playerId, attackerIndices, blockerIndices);
    }

    private ManaPool buildVirtualManaPool(GameData gd, UUID playerId) {
        ManaPool virtual = new ManaPool();
        ManaPool current = gd.playerManaPools.get(playerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                virtual.add(color, current.get(color));
                virtual.addCreatureMana(color, current.getCreatureMana(color));
            }
        }
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (Permanent perm : battlefield) {
            if (perm.isTapped()) continue;
            boolean isCreature = gameQueryService.isCreature(gd, perm);
            if (isCreature && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                if (effect instanceof AwardManaEffect me) {
                    virtual.add(me.color(), me.amount());
                    if (isCreature) virtual.addCreatureMana(me.color(), me.amount());
                } else if (effect instanceof AwardAnyColorManaEffect) {
                    virtual.add(ManaColor.COLORLESS);
                    if (isCreature) virtual.addCreatureMana(ManaColor.COLORLESS, 1);
                }
            }
        }
        return virtual;
    }

    private void tapLandsForCard(GameData gd, UUID playerId, Card card, int xValue) {
        if (card.getManaCost() == null) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool currentPool = gd.playerManaPools.get(playerId);

        if (card.isRequiresCreatureMana()) {
            if (cost.canPayCreatureOnly(currentPool)) return;
            Player player = new Player(playerId, "sim");
            List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent perm = battlefield.get(i);
                if (perm.isTapped()) continue;
                if (!gameQueryService.isCreature(gd, perm)) continue;
                if (perm.isSummoningSick()
                        && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
                boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                        .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
                if (!producesMana) continue;
                gameService.tapPermanent(gd, player, i);
                currentPool = gd.playerManaPools.get(playerId);
                if (cost.canPayCreatureOnly(currentPool)) return;
            }
            return;
        }

        boolean alreadyPaid;
        if (cost.hasX() && card.getXColorRestriction() != null) {
            alreadyPaid = cost.canPay(currentPool, xValue, card.getXColorRestriction(), 0);
        } else {
            alreadyPaid = cost.canPay(currentPool, xValue);
        }
        if (alreadyPaid) return;

        Player player = new Player(playerId, "sim");
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) continue;
            if (gameQueryService.isCreature(gd, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
            boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
            if (!producesMana) continue;
            gameService.tapPermanent(gd, player, i);
            currentPool = gd.playerManaPools.get(playerId);
            boolean canPayNow;
            if (cost.hasX() && card.getXColorRestriction() != null) {
                canPayNow = cost.canPay(currentPool, xValue, card.getXColorRestriction(), 0);
            } else {
                canPayNow = cost.canPay(currentPool, xValue);
            }
            if (canPayNow) return;
        }
    }

    private int calculateSmartX(GameData gd, Card card, UUID targetId, ManaPool virtualPool) {
        ManaCost cost = new ManaCost(card.getManaCost());
        int maxX;
        if (card.getXColorRestriction() != null) {
            maxX = cost.calculateMaxX(virtualPool, card.getXColorRestriction(), 0);
        } else {
            maxX = cost.calculateMaxX(virtualPool);
        }
        if (maxX <= 0) {
            return 0;
        }

        if (targetId != null) {
            Permanent target = gameQueryService.findPermanentById(gd, targetId);
            if (target != null && gameQueryService.isCreature(gd, target)) {
                int toughness = gameQueryService.getEffectiveToughness(gd, target);
                return Math.min(toughness, maxX);
            }
        }

        return maxX;
    }

    private UUID findBestTarget(GameData gd, Card card, UUID playerId) {
        UUID opponentId = getOpponentId(gd, playerId);

        // Handle player-only targeting (e.g. Haunting Echoes, Mind Rot)
        Set<TargetType> allowedTargets = card.getAllowedTargets();
        if (allowedTargets.contains(TargetType.PLAYER) && !allowedTargets.contains(TargetType.PERMANENT)) {
            return opponentId;
        }

        // Handle graveyard targeting (e.g. Unburial Rites, Gruesome Encore)
        if (allowedTargets.contains(TargetType.GRAVEYARD)) {
            return findBestGraveyardTarget(gd, card, playerId, opponentId);
        }

        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());

        // Prefer creatures that pass the target filter
        UUID creatureTarget = oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gd, p))
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gd, p)))
                .map(Permanent::getId)
                .orElse(null);
        if (creatureTarget != null) {
            return creatureTarget;
        }

        // Fall back to any permanent that passes the target filter (e.g., artifacts/enchantments for Naturalize)
        return oppBattlefield.stream()
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .findFirst()
                .map(Permanent::getId)
                .orElse(null);
    }

    private UUID findBestGraveyardTarget(GameData gd, Card card, UUID playerId, UUID opponentId) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!effect.canTargetGraveyard()) continue;

            List<Card> candidates;
            if (effect instanceof ReturnCardFromGraveyardEffect rge) {
                candidates = getSimGraveyardCandidates(gd, rge.source(), playerId, opponentId);
                if (rge.filter() != null) {
                    candidates = candidates.stream()
                            .filter(c -> gameQueryService.matchesCardPredicate(c, rge.filter(), card.getId()))
                            .toList();
                }
            } else {
                GraveyardSearchScope scope = effect.canTargetAnyGraveyard()
                        ? GraveyardSearchScope.ALL_GRAVEYARDS
                        : GraveyardSearchScope.OPPONENT_GRAVEYARD;
                candidates = getSimGraveyardCandidates(gd, scope, playerId, opponentId);
            }

            if (!candidates.isEmpty()) {
                return candidates.stream()
                        .max(Comparator.comparingInt(Card::getManaValue))
                        .map(Card::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    private List<Card> getSimGraveyardCandidates(GameData gd, GraveyardSearchScope scope,
                                                  UUID playerId, UUID opponentId) {
        List<Card> candidates = new ArrayList<>();
        switch (scope) {
            case CONTROLLERS_GRAVEYARD -> candidates.addAll(
                    gd.playerGraveyards.getOrDefault(playerId, List.of()));
            case OPPONENT_GRAVEYARD -> candidates.addAll(
                    gd.playerGraveyards.getOrDefault(opponentId, List.of()));
            case ALL_GRAVEYARDS -> {
                for (UUID pid : gd.orderedPlayerIds) {
                    candidates.addAll(gd.playerGraveyards.getOrDefault(pid, List.of()));
                }
            }
        }
        return candidates;
    }

    private boolean passesTargetFilter(GameData gd, Card card, Permanent target, UUID controllerId) {
        if (card.getTargetFilter() == null) {
            return true;
        }
        try {
            gameQueryService.validateTargetFilter(card.getTargetFilter(), target,
                    FilterContext.of(gd)
                            .withSourceCardId(card.getId())
                            .withSourceControllerId(controllerId));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private int findPermanentIndex(GameData gd, UUID playerId, UUID permanentId) {
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getId().equals(permanentId)) return i;
        }
        return -1;
    }

    private Map<UUID, Integer> autoAssignCombatDamage(InteractionContext.CombatDamageAssignment cda) {
        Map<UUID, Integer> assignments = new HashMap<>();
        int remaining = cda.totalDamage();
        for (var target : cda.validTargets()) {
            if (target.isPlayer()) continue;
            int lethal = cda.isDeathtouch()
                    ? Math.max(0, 1 - target.currentDamage())
                    : target.effectiveToughness() - target.currentDamage();
            int dmg = Math.min(remaining, lethal);
            if (dmg > 0) {
                assignments.put(target.id(), dmg);
                remaining -= dmg;
            }
        }
        if (remaining > 0) {
            for (var target : cda.validTargets()) {
                if (target.isPlayer()) {
                    assignments.put(target.id(), remaining);
                    break;
                }
            }
        }
        return assignments;
    }

    private UUID getOpponentId(GameData gd, UUID playerId) {
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(playerId)) return id;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void scanEffectHandlers(Object bean, EffectHandlerRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            HandlesEffect annotation = method.getAnnotation(HandlesEffect.class);
            if (annotation == null) continue;
            method.setAccessible(true);
            Class<?>[] params = method.getParameterTypes();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
                if (params.length == 3
                        && params[0] == GameData.class
                        && params[1] == com.github.laxika.magicalvibes.model.StackEntry.class
                        && CardEffect.class.isAssignableFrom(params[2])) {
                    Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[2];
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry, effectParam.cast(effect)); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 2
                        && params[0] == GameData.class
                        && params[1] == com.github.laxika.magicalvibes.model.StackEntry.class) {
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void scanTargetValidators(Object bean, TargetValidatorRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            ValidatesTarget annotation = method.getAnnotation(ValidatesTarget.class);
            if (annotation == null) continue;
            method.setAccessible(true);
            Class<?>[] params = method.getParameterTypes();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
                if (params.length == 2
                        && params[0] == TargetValidationContext.class
                        && CardEffect.class.isAssignableFrom(params[1])) {
                    Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];
                    registry.register(annotation.value(), (ctx, effect) -> {
                        try { handle.invoke(ctx, effectParam.cast(effect)); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 1
                        && params[0] == TargetValidationContext.class) {
                    registry.register(annotation.value(), (ctx, effect) -> {
                        try { handle.invoke(ctx); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void scanStaticEffectHandlers(Object bean, StaticEffectHandlerRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            HandlesStaticEffect annotation = method.getAnnotation(HandlesStaticEffect.class);
            if (annotation == null) continue;
            method.setAccessible(true);
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);
                if (annotation.selfOnly()) {
                    registry.registerSelfHandler(annotation.value(), (context, effect, accumulator) -> {
                        try { handle.invoke(context, effect, accumulator); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else {
                    registry.register(annotation.value(), (context, effect, accumulator) -> {
                        try { handle.invoke(context, effect, accumulator); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
