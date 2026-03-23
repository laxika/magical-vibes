package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.handler.GameMessageHandler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
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
import com.github.laxika.magicalvibes.service.battlefield.ExileResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.exile.ExileEggCounterResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileReturnResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.library.LibraryRevealResolutionService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleResolutionService;
import com.github.laxika.magicalvibes.service.library.MillResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.LobbyService;
import com.github.laxika.magicalvibes.service.PreventionResolutionService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.ability.ActivatedAbilityExecutionService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.ReconnectionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.turn.TurnResolutionService;
import com.github.laxika.magicalvibes.service.turn.AutoPassService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import com.github.laxika.magicalvibes.service.TargetRedirectionResolutionService;
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
import com.github.laxika.magicalvibes.service.effect.AnimationResolutionService;
import com.github.laxika.magicalvibes.service.effect.BoostResolutionService;
import com.github.laxika.magicalvibes.service.effect.CombatRestrictionResolutionService;
import com.github.laxika.magicalvibes.service.effect.KeywordGrantResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentCounterResolutionService;
import com.github.laxika.magicalvibes.service.effect.TapUntapResolutionService;
import com.github.laxika.magicalvibes.service.effect.CardSpecificResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EquipResolutionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.service.effect.HandlesStaticEffect;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import com.github.laxika.magicalvibes.service.effect.WinConditionResolutionService;
import com.github.laxika.magicalvibes.service.validate.BounceTargetValidators;
import com.github.laxika.magicalvibes.service.validate.CreatureModTargetValidators;
import com.github.laxika.magicalvibes.service.validate.DamageTargetValidators;
import com.github.laxika.magicalvibes.service.validate.DestructionTargetValidators;
import com.github.laxika.magicalvibes.service.validate.ExileTargetValidators;
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
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GameTestHarness {

    private static boolean oracleLoaded = false;

    private final GameRegistry gameRegistry;
    private final WebSocketSessionManager sessionManager;
    private final GameService gameService;
    private final GameQueryService gameQueryService;
    private final TargetValidationService targetValidationService;
    private final MessageHandler messageHandler;
    private final LobbyService lobbyService;
    private final GameData gameData;
    private final Player player1;
    private final Player player2;
    private final FakeConnection conn1;
    private final FakeConnection conn2;
    private final LegendRuleService legendRuleService;
    private final PermanentRemovalService permanentRemovalService;
    private final StackResolutionService stackResolutionService;
    private final DrawService drawService;
    private final PlayerInputService playerInputService;
    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;
    private final SpellCastingService spellCastingService;
    private final CombatAttackService combatAttackService;
    private final StateBasedActionService stateBasedActionService;
    private final LifeResolutionService lifeResolutionService;

    public GameTestHarness() {
        if (!oracleLoaded) {
            ScryfallOracleLoader.loadAll("./scryfall-cache");
            oracleLoaded = true;
        }

        gameRegistry = new GameRegistry();
        sessionManager = new WebSocketSessionManager(new JacksonConfig().objectMapper());
        CardViewFactory cardViewFactory = new CardViewFactory();
        PermanentViewFactory permanentViewFactory = new PermanentViewFactory(cardViewFactory);
        StackEntryViewFactory stackEntryViewFactory = new StackEntryViewFactory(cardViewFactory);
        StaticEffectHandlerRegistry staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        gameQueryService = new GameQueryService(staticEffectHandlerRegistry);
        StaticEffectResolutionService staticEffectResolutionService = new StaticEffectResolutionService(gameQueryService, staticEffectHandlerRegistry);
        scanStaticEffectHandlers(staticEffectResolutionService, staticEffectHandlerRegistry);
        playerInputService = new PlayerInputService(sessionManager, cardViewFactory);
        ValidTargetService validTargetService = new ValidTargetService(gameQueryService);
        gameBroadcastService = new GameBroadcastService(
                sessionManager, cardViewFactory, permanentViewFactory, stackEntryViewFactory, gameQueryService, validTargetService);
        DraftRegistry draftRegistry = new DraftRegistry();
        legendRuleService = new LegendRuleService(playerInputService, gameQueryService);
        TriggeredAbilityQueueService triggeredAbilityQueueService = new TriggeredAbilityQueueService(
                gameQueryService, gameBroadcastService, playerInputService, cardViewFactory);
        CreatureControlService creatureControlService = new CreatureControlService(gameBroadcastService, gameQueryService);
        DamagePreventionService damagePreventionService = new DamagePreventionService(gameQueryService);
        GameOutcomeService gameOutcomeService = new GameOutcomeService(gameQueryService, gameBroadcastService, sessionManager, gameRegistry, draftRegistry, null);
        DeathTriggerService deathTriggerService = new DeathTriggerService(gameQueryService, gameBroadcastService);
        drawService = new DrawService(gameQueryService, gameBroadcastService, gameOutcomeService, triggeredAbilityQueueService);
        battlefieldEntryService = new BattlefieldEntryService(gameQueryService, gameBroadcastService, playerInputService, cardViewFactory, null, null);
        CloneService cloneService = new CloneService(gameQueryService, gameBroadcastService, playerInputService, legendRuleService, battlefieldEntryService);
        battlefieldEntryService.setCloneService(cloneService);
        WarpWorldService warpWorldService = new WarpWorldService(gameQueryService, gameBroadcastService, playerInputService, battlefieldEntryService, legendRuleService, creatureControlService, cardViewFactory, sessionManager);
        ExileService exileService = new ExileService();
        GraveyardService graveyardService = new GraveyardService(gameQueryService, gameBroadcastService, exileService, null);
        AuraAttachmentService auraAttachmentService = new AuraAttachmentService(gameQueryService, gameBroadcastService, graveyardService, deathTriggerService);
        permanentRemovalService = new PermanentRemovalService(
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
        triggerCollectionService = new TriggerCollectionService(
                triggerCollectorRegistry, gameOutcomeService, playerInputService, triggeredAbilityQueueService, gameQueryService, gameBroadcastService);
        graveyardService.setTriggerCollectionService(triggerCollectionService);
        StateTriggerService stateTriggerService = new StateTriggerService(gameBroadcastService);
        stateBasedActionService = new StateBasedActionService(
                gameOutcomeService, gameQueryService, gameBroadcastService, permanentRemovalService, graveyardService, stateTriggerService);
        lifeResolutionService = new LifeResolutionService(gameQueryService, gameBroadcastService, playerInputService, triggerCollectionService);
        CombatTriggerService combatTriggerService = new CombatTriggerService(gameBroadcastService);
        combatAttackService = new CombatAttackService(gameQueryService, gameBroadcastService, sessionManager, triggerCollectionService, combatTriggerService);
        CombatBlockService combatBlockService = new CombatBlockService(gameQueryService, gameBroadcastService, sessionManager, combatAttackService, combatTriggerService);
        CombatDamageService combatDamageService = new CombatDamageService(gameQueryService, gameBroadcastService, gameOutcomeService, damagePreventionService, graveyardService, deathTriggerService, permanentRemovalService, playerInputService, sessionManager, triggerCollectionService, lifeResolutionService, combatAttackService, combatTriggerService);
        CombatService combatService = new CombatService(
                combatAttackService, combatBlockService, combatDamageService, gameBroadcastService, permanentRemovalService, battlefieldEntryService);
        TargetValidatorRegistry targetValidatorRegistry = new TargetValidatorRegistry();
        this.targetValidationService = new TargetValidationService(gameQueryService, targetValidatorRegistry);
        List<Object> validatorBeans = List.of(
                new DamageTargetValidators(targetValidationService, gameQueryService),
                new CreatureModTargetValidators(targetValidationService),
                new DestructionTargetValidators(targetValidationService, gameQueryService),
                new GraveyardTargetValidators(targetValidationService, gameQueryService),
                new ExileTargetValidators(gameQueryService),
                new BounceTargetValidators(targetValidationService),
                new LibraryTargetValidators(targetValidationService),
                new PermanentControlTargetValidators(targetValidationService, gameQueryService),
                new LifeTargetValidators(targetValidationService)
        );
        for (Object bean : validatorBeans) {
            scanTargetValidators(bean, targetValidatorRegistry);
        }
        TargetLegalityService targetLegalityService = new TargetLegalityService(gameQueryService, targetValidationService);
        battlefieldEntryService.setTargetLegalityService(targetLegalityService);
        EffectHandlerRegistry effectHandlerRegistry = new EffectHandlerRegistry();
        DamageResolutionService damageResolutionService = new DamageResolutionService(graveyardService, damagePreventionService, gameOutcomeService, gameQueryService, gameBroadcastService, permanentRemovalService, triggerCollectionService, lifeResolutionService);
        ExileResolutionService exileResolutionService = new ExileResolutionService(graveyardService, gameQueryService, gameBroadcastService, permanentRemovalService, playerInputService, cardViewFactory, triggerCollectionService, battlefieldEntryService, exileService);
        PlayerInteractionResolutionService playerInteractionResolutionService = new PlayerInteractionResolutionService(drawService, graveyardService, gameQueryService, gameBroadcastService, playerInputService, sessionManager, cardViewFactory, permanentRemovalService, battlefieldEntryService, triggerCollectionService, effectHandlerRegistry);
        TurnCleanupService turnCleanupService = new TurnCleanupService(auraAttachmentService);
        DestructionResolutionService destructionResolutionService = new DestructionResolutionService(battlefieldEntryService, graveyardService, damagePreventionService, gameOutcomeService, permanentRemovalService, gameQueryService, gameBroadcastService, playerInputService, lifeResolutionService);
        PermanentControlResolutionService permanentControlResolutionService = new PermanentControlResolutionService(battlefieldEntryService, legendRuleService, gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService, triggerCollectionService, creatureControlService);
        miscTriggerCollectorService.setPermanentControlResolutionService(permanentControlResolutionService);
        LibrarySearchResolutionService librarySearchResolutionService = new LibrarySearchResolutionService(drawService, gameBroadcastService, sessionManager, cardViewFactory, gameQueryService, permanentRemovalService, playerInputService);
        GraveyardReturnResolutionService graveyardReturnResolutionService = new GraveyardReturnResolutionService(battlefieldEntryService, permanentRemovalService, legendRuleService, gameQueryService, gameBroadcastService, playerInputService, lifeResolutionService, exileService, cardViewFactory);
        PermanentCounterResolutionService permanentCounterResolutionService = new PermanentCounterResolutionService(gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService);
        List<Object> effectServices = List.of(
                damageResolutionService,
                destructionResolutionService,
                new MillResolutionService(graveyardService, gameBroadcastService, gameQueryService, permanentControlResolutionService),
                new LibraryShuffleResolutionService(gameBroadcastService, gameQueryService, permanentRemovalService),
                librarySearchResolutionService,
                new LibraryRevealResolutionService(gameQueryService, gameBroadcastService, sessionManager, cardViewFactory, battlefieldEntryService, exileService),
                new PreventionResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new CounterResolutionService(graveyardService, exileService, gameBroadcastService, gameQueryService, stateTriggerService),
                exileResolutionService,
                new CopyResolutionService(gameBroadcastService, validTargetService, gameQueryService),
                new TargetRedirectionResolutionService(gameQueryService, gameBroadcastService, playerInputService, targetLegalityService),
                graveyardReturnResolutionService,
                new ExileReturnResolutionService(gameQueryService, gameBroadcastService),
                new ExileEggCounterResolutionService(gameQueryService, gameBroadcastService, battlefieldEntryService),
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
                new CardSpecificResolutionService(graveyardService, warpWorldService, battlefieldEntryService, gameQueryService, gameBroadcastService, sessionManager, cardViewFactory, permanentRemovalService, legendRuleService, exileService),
                new WinConditionResolutionService(gameOutcomeService, gameBroadcastService, gameQueryService)
        );
        for (Object service : effectServices) {
            scanEffectHandlers(service, effectHandlerRegistry);
        }
        EffectResolutionService effectResolutionService = new EffectResolutionService(gameQueryService, effectHandlerRegistry, gameBroadcastService, permanentRemovalService);
        stackResolutionService = new StackResolutionService(
                battlefieldEntryService, cloneService, graveyardService, legendRuleService, stateBasedActionService, gameQueryService, targetLegalityService,
                gameBroadcastService, effectResolutionService, playerInputService, triggerCollectionService, creatureControlService, stateTriggerService, exileService);
        UntapStepService untapStepService = new UntapStepService(gameQueryService, gameBroadcastService);
        StepTriggerService stepTriggerService = new StepTriggerService(drawService, gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService, battlefieldEntryService, triggerCollectionService);
        AutoPassService autoPassService = new AutoPassService(gameQueryService, gameBroadcastService, triggerCollectionService, stackResolutionService, stepTriggerService);
        TurnProgressionService turnProgressionService = new TurnProgressionService(
                combatService, gameBroadcastService, playerInputService, turnCleanupService, untapStepService, stepTriggerService, autoPassService);
        ActivatedAbilityExecutionService activatedAbilityExecutionService = new ActivatedAbilityExecutionService(
                damagePreventionService, permanentRemovalService, triggerCollectionService, stateBasedActionService, gameQueryService, gameBroadcastService, playerInputService, sessionManager, lifeResolutionService);
        AbilityActivationService abilityActivationService = new AbilityActivationService(
                graveyardService, gameQueryService, gameBroadcastService, targetLegalityService, activatedAbilityExecutionService,
                playerInputService, sessionManager, permanentRemovalService, triggerCollectionService, exileService);
        spellCastingService = new SpellCastingService(
                battlefieldEntryService, gameQueryService, gameBroadcastService, turnProgressionService, targetLegalityService, permanentRemovalService, triggerCollectionService);
        ChoiceHandlerService listChoiceHandlerService = new ChoiceHandlerService(
                sessionManager, gameQueryService, warpWorldService, battlefieldEntryService, gameBroadcastService,
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
                gameQueryService, battlefieldEntryService, legendRuleService, gameBroadcastService, turnProgressionService, permanentRemovalService, triggerCollectionService, playerInputService, lifeResolutionService, exileService, graveyardReturnResolutionService, inputCompletionService, effectResolutionService);
        MayCastHandlerService mayCastHandlerService = new MayCastHandlerService(
                inputCompletionService, gameQueryService, graveyardService, gameBroadcastService, playerInputService, permanentRemovalService, triggerCollectionService, battlefieldEntryService, exileService);
        MayCopyHandlerService mayCopyHandlerService = new MayCopyHandlerService(
                inputCompletionService, gameQueryService, cloneService, stateBasedActionService, gameBroadcastService, playerInputService, turnProgressionService, targetLegalityService, triggerCollectionService);
        MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService = new MayPenaltyChoiceHandlerService(
                inputCompletionService, gameQueryService, graveyardService, exileService, stateTriggerService, drawService, gameBroadcastService, playerInputService, turnProgressionService, stateBasedActionService, permanentRemovalService);
        MulliganService mulliganService = new MulliganService(
                sessionManager, gameBroadcastService, turnProgressionService, battlefieldEntryService, playerInputService);
        MayMiscHandlerService mayMiscHandlerService = new MayMiscHandlerService(
                inputCompletionService, gameQueryService, drawService, gameBroadcastService, mulliganService, playerInputService, turnProgressionService, battlefieldEntryService, sessionManager);
        MayAbilityHandlerService mayAbilityHandlerService = new MayAbilityHandlerService(
                inputCompletionService, mayCastHandlerService, mayCopyHandlerService, mayPenaltyChoiceHandlerService, mayMiscHandlerService,
                gameQueryService, gameBroadcastService, playerInputService, turnProgressionService, effectResolutionService, destructionResolutionService, graveyardReturnResolutionService);
        XValueChoiceHandlerService xValueChoiceHandlerService = new XValueChoiceHandlerService(
                gameBroadcastService, stateBasedActionService,
                playerInputService, turnProgressionService, effectResolutionService);
        LibraryChoiceHandlerService libraryChoiceHandlerService = new LibraryChoiceHandlerService(
                sessionManager, gameQueryService, graveyardService, warpWorldService, battlefieldEntryService, legendRuleService, stateBasedActionService, gameBroadcastService,
                cardViewFactory, turnProgressionService, playerInputService, effectResolutionService, exileService);
        ReconnectionService reconnectionService = new ReconnectionService(
                sessionManager, cardViewFactory, combatService, gameQueryService);
        gameService = new GameService(
                gameRegistry, gameQueryService, gameBroadcastService,
                combatService,
                turnProgressionService,
                listChoiceHandlerService, cardChoiceHandlerService,
                permanentChoiceHandlerService, graveyardChoiceHandlerService,
                mayAbilityHandlerService, xValueChoiceHandlerService, libraryChoiceHandlerService,
                spellCastingService,
                stackResolutionService, abilityActivationService, mulliganService, reconnectionService,
                exileResolutionService, gameOutcomeService);
        lobbyService = new LobbyService(gameRegistry, gameBroadcastService);

        // Create the MessageHandler (GameMessageHandler) for AI tests
        messageHandler = new GameMessageHandler(
                null, gameService, gameBroadcastService, lobbyService, gameRegistry,
                sessionManager, new JacksonConfig().objectMapper(),
                null, null, draftRegistry, null, validTargetService);

        player1 = new Player(UUID.randomUUID(), "Alice");
        player2 = new Player(UUID.randomUUID(), "Bob");
        conn1 = new FakeConnection("conn-1");
        conn2 = new FakeConnection("conn-2");

        sessionManager.registerPlayer(conn1, player1.getId(), player1.getUsername());
        sessionManager.registerPlayer(conn2, player2.getId(), player2.getUsername());

        lobbyService.createGame("Test Game", player1, "cho-mannos-resolve");
        GameData gd = gameRegistry.getGameForPlayer(player1.getId());
        lobbyService.joinGame(gd, player2, "cho-mannos-resolve");

        this.gameData = gameRegistry.getGameForPlayer(player1.getId());

        // Force player1 as starting player for deterministic tests
        this.gameData.startingPlayerId = player1.getId();
    }

    public void skipMulligan() {
        gameService.keepHand(gameData, player1);
        gameService.keepHand(gameData, player2);
    }

    public void setHand(Player player, List<Card> cards) {
        gameData.playerHands.put(player.getId(), new ArrayList<>(cards));
    }

    public void addMana(Player player, ManaColor color, int amount) {
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        for (int i = 0; i < amount; i++) {
            pool.add(color);
        }
    }

    public void addCreatureMana(Player player, ManaColor color, int amount) {
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        for (int i = 0; i < amount; i++) {
            pool.add(color);
        }
        pool.addCreatureMana(color, amount);
    }

    public void addToBattlefield(Player player, Card card) {
        gameData.playerBattlefields.get(player.getId()).add(new Permanent(card));
    }

    public Permanent addToBattlefieldAndReturn(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gameData.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    public void runStateBasedActions() {
        stateBasedActionService.performStateBasedActions(gameData);
    }

    public void setGraveyard(Player player, List<Card> cards) {
        gameData.playerGraveyards.put(player.getId(), new ArrayList<>(cards));
    }

    public void setExile(Player player, List<Card> cards) {
        for (Card card : cards) {
            gameData.addToExile(player.getId(), card);
        }
    }

    public void setLife(Player player, int life) {
        gameData.playerLifeTotals.put(player.getId(), life);
    }

    public void castCreature(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castCreature(Player player, int cardIndex, int mode) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, mode, null, null);
    }

    public void castCreature(Player player, int cardIndex, int mode, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, mode, targetId, null);
    }

    public void castCreature(Player player, int cardIndex, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetIds, List.of());
    }

    public void castKickedCreature(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, null, null, null, null, null, true);
    }

    public void castKickedCreature(Player player, int cardIndex, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, null, null, null, null, null, true);
    }

    public void castKickedInstant(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, null, null, null, null, null, true);
    }

    public void castKickedInstant(Player player, int cardIndex, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, null, null, null, null, null, true);
    }

    public void castKickedInstantWithSacrifice(Player player, int cardIndex, UUID targetId, UUID sacrificePermanentId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, sacrificePermanentId, null, null, null, null, true);
    }

    public void castKickedSorcery(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, null, null, null, null, null, true);
    }

    public void castKickedSorcery(Player player, int cardIndex, Map<UUID, Integer> damageAssignments) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, damageAssignments, List.of(), List.of(), false, null, null, null, null, null, true);
    }

    public void castKickedSorceryWithSacrifice(Player player, int cardIndex, UUID targetId, UUID kickerTargetId, UUID sacrificePermanentId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(kickerTargetId), List.of(), false, sacrificePermanentId, null, null, null, null, true);
    }

    public void castKickedSorceryWithSacrificeNoKickerTarget(Player player, int cardIndex, UUID targetId, UUID sacrificePermanentId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, sacrificePermanentId, null, null, null, null, true);
    }

    public void castCreatureWithGraveyardExile(Player player, int cardIndex, int exileGraveyardCardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, null, null, null, exileGraveyardCardIndex);
    }

    public void castCreatureWithMultipleGraveyardExile(Player player, int cardIndex, List<Integer> exileGraveyardCardIndices) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, null, null, null, null, exileGraveyardCardIndices);
    }

    public void castFromGraveyard(Player player, int graveyardCardIndex) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, null, null);
    }

    public void castFromGraveyard(Player player, int graveyardCardIndex, CardType chosenGraveyardType) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, null, null, List.of(), null, chosenGraveyardType);
    }

    public void castFromGraveyard(Player player, int graveyardCardIndex, List<Integer> exileGraveyardCardIndices) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, null, null, List.of(), exileGraveyardCardIndices);
    }

    public void castFromExile(Player player, UUID exileCardId) {
        ensurePriority(player);
        gameService.playCardFromExile(gameData, player, exileCardId, null, null);
    }

    public void castCreatureWithAlternateCost(Player player, int cardIndex, List<UUID> sacrificePermanentIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, null, null, sacrificePermanentIds);
    }

    public void castCreatureWithSacrificeForReduction(Player player, int cardIndex, UUID targetId, List<UUID> sacrificePermanentIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, null, null, sacrificePermanentIds);
    }

    public void castEnchantment(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castEnchantment(Player player, int cardIndex, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null);
    }

    public void castArtifact(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castArtifact(Player player, int cardIndex, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null);
    }

    public void playGraveyardLand(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), true);
    }

    public void castPlaneswalker(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castSorcery(Player player, int cardIndex, int xValue) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, xValue, null, null);
    }

    public void castSorcery(Player player, int cardIndex, UUID targetPlayerId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetPlayerId, null);
    }

    public void castSorcery(Player player, int cardIndex, int xValue, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, xValue, targetId, null);
    }

    public void castSorcery(Player player, int cardIndex, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetIds, List.of());
    }

    public void castSorcery(Player player, int cardIndex, UUID targetId, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, targetIds, List.of());
    }

    public void castSorcery(Player player, int cardIndex, int xValue, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, xValue, null, null, targetIds, List.of());
    }

    public void castSorceryWithSacrifice(Player player, int cardIndex, UUID sacrificePermanentId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, sacrificePermanentId);
    }

    public void castSorceryWithSacrifice(Player player, int cardIndex, UUID targetId, UUID sacrificePermanentId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, sacrificePermanentId);
    }

    public void castInstant(Player player, int cardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castInstant(Player player, int cardIndex, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null);
    }

    public void castInstant(Player player, int cardIndex, int xValue, UUID targetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, xValue, targetId, null);
    }

    public void castInstant(Player player, int cardIndex, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetIds, List.of());
    }

    public void castInstant(Player player, int cardIndex, UUID spellTargetId, UUID permanentTargetId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, spellTargetId, null, List.of(permanentTargetId), List.of());
    }

    public void castInstant(Player player, int cardIndex, Map<UUID, Integer> damageAssignments) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, damageAssignments);
    }

    public void castInstantWithSacrifice(Player player, int cardIndex, UUID targetId, UUID sacrificePermanentId) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, sacrificePermanentId);
    }

    public void castInstantWithGraveyardExile(Player player, int cardIndex, UUID targetId, int exileGraveyardCardIndex) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, null, null, null, exileGraveyardCardIndex);
    }

    public void castInstantWithMultipleGraveyardExile(Player player, int cardIndex, UUID targetId, List<Integer> exileGraveyardCardIndices) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, targetId, null, List.of(), List.of(), false, null, null, null, null, exileGraveyardCardIndices);
    }

    public void castInstantWithConvoke(Player player, int cardIndex, List<UUID> targetIds, List<UUID> convokeCreatureIds) {
        ensurePriority(player);
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetIds, convokeCreatureIds);
    }

    public void castFromLibraryTop(Player player) {
        ensurePriority(player);
        gameService.playCardFromLibraryTop(gameData, player, null, null);
    }

    public void castFromLibraryTop(Player player, UUID targetId) {
        ensurePriority(player);
        gameService.playCardFromLibraryTop(gameData, player, null, targetId);
    }

    public void castAndResolveFromLibraryTop(Player player) {
        castFromLibraryTop(player);
        passBothPriorities();
    }

    public void castAndResolveFromLibraryTop(Player player, UUID targetId) {
        castFromLibraryTop(player, targetId);
        passBothPriorities();
    }

    public void castFlashback(Player player, int graveyardCardIndex, UUID targetId) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, null, targetId);
    }

    public void castFlashback(Player player, int graveyardCardIndex) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, null, (UUID) null);
    }

    public void castFlashback(Player player, int graveyardCardIndex, int xValue, UUID targetId) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId);
    }

    public void castFlashback(Player player, int graveyardCardIndex, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.playFlashbackSpell(gameData, player, graveyardCardIndex, null, null, targetIds);
    }

    public void castAndResolveFlashback(Player player, int graveyardCardIndex, UUID targetId) {
        castFlashback(player, graveyardCardIndex, targetId);
        passBothPriorities();
    }

    public void castAndResolveInstant(Player player, int cardIndex) {
        castInstant(player, cardIndex);
        passBothPriorities();
    }

    public void castAndResolveInstant(Player player, int cardIndex, UUID targetId) {
        castInstant(player, cardIndex, targetId);
        passBothPriorities();
    }

    public void castAndResolveInstant(Player player, int cardIndex, List<UUID> targetIds) {
        castInstant(player, cardIndex, targetIds);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, int xValue) {
        castSorcery(player, cardIndex, xValue);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, UUID targetPlayerId) {
        castSorcery(player, cardIndex, targetPlayerId);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, int xValue, UUID targetId) {
        castSorcery(player, cardIndex, xValue, targetId);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, List<UUID> targetIds) {
        castSorcery(player, cardIndex, targetIds);
        passBothPriorities();
    }

    public void tapPermanent(Player player, int permanentIndex) {
        ensurePriority(player);
        gameService.tapPermanent(gameData, player, permanentIndex);
    }

    public void sacrificePermanent(Player player, int permanentIndex, UUID targetId) {
        ensurePriority(player);
        gameService.sacrificePermanent(gameData, player, permanentIndex, targetId);
    }

    public void activateAbility(Player player, int permanentIndex, Integer xValue, UUID targetId) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, 0, xValue, targetId, null);
    }

    public void activateAbility(Player player, int permanentIndex, Integer xValue, UUID targetId, Zone Zone) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, 0, xValue, targetId, Zone);
    }

    public void activateAbility(Player player, int permanentIndex, int abilityIndex, Integer xValue, UUID targetId) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, null);
    }

    public void activateAbility(Player player, int permanentIndex, int abilityIndex, Integer xValue, UUID targetId, Zone targetZone) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone);
    }

    public void activateAbilityWithDamageAssignments(Player player, int permanentIndex, int abilityIndex, Integer xValue, Map<UUID, Integer> damageAssignments) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, null, null, null, damageAssignments);
    }

    public void activateAbilityWithMultiTargets(Player player, int permanentIndex, int abilityIndex, List<UUID> targetIds) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, null, null, null, targetIds);
    }

    public void activateAbilityWithGraveyardTargets(Player player, int permanentIndex, int abilityIndex, List<UUID> graveyardCardIds) {
        ensurePriority(player);
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, null, null, Zone.GRAVEYARD, graveyardCardIds);
    }

    public void activateGraveyardAbility(Player player, int graveyardCardIndex) {
        ensurePriority(player);
        gameService.activateGraveyardAbility(gameData, player, graveyardCardIndex, 0);
    }

    public void activateGraveyardAbility(Player player, int graveyardCardIndex, int abilityIndex) {
        ensurePriority(player);
        gameService.activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex);
    }

    public void handlePermanentChosen(Player player, UUID permanentId) {
        gameService.handlePermanentChosen(gameData, player, permanentId);
    }

    public void handleMultiplePermanentsChosen(Player player, List<UUID> permanentIds) {
        gameService.handleMultiplePermanentsChosen(gameData, player, permanentIds);
    }

    public void handleMultipleGraveyardCardsChosen(Player player, List<UUID> cardIds) {
        gameService.handleMultipleGraveyardCardsChosen(gameData, player, cardIds);
    }

    public UUID getPermanentId(Player player, String cardName) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().getName().equals(cardName)) {
                return p.getId();
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }

    public void passPriority(Player player) {
        gameService.passPriority(gameData, player);
    }

    public void passBothPriorities() {
        // CR 603.5: If already awaiting input (e.g. may ability prompt), return immediately.
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        // Reset priority so active player gets first chance
        gameData.priorityPassedBy.clear();

        // Determine priority order based on active player
        Player first, second;
        if (gameData.activePlayerId != null && gameData.activePlayerId.equals(player2.getId())) {
            first = player2;
            second = player1;
        } else {
            first = player1;
            second = player2;
        }

        TurnStep stepBefore = gameData.currentStep;
        int stackSizeBefore = gameData.stack.size();

        gameService.passPriority(gameData, first);

        // After auto-pass rework, the first pass may trigger an auto-pass cascade
        // that handles the second player too (advancing the step or resolving the stack).
        // Also stop if the game entered an awaiting-input state (e.g. may ability prompt).
        if (gameData.currentStep != stepBefore || gameData.stack.size() != stackSizeBefore
                || gameData.interaction.isAwaitingInput()) {
            return;
        }

        gameService.passPriority(gameData, second);
    }

    public void handleCardChosen(Player player, int cardIndex) {
        gameService.handleCardChosen(gameData, player, cardIndex);
    }

    public void handleGraveyardCardChosen(Player player, int cardIndex) {
        gameService.handleGraveyardCardChosen(gameData, player, cardIndex);
    }

    public void handleListChoice(Player player, String choiceName) {
        gameService.handleListChoice(gameData, player, choiceName);
    }

    public void handleMayAbilityChosen(Player player, boolean accepted) {
        gameService.handleMayAbilityChosen(gameData, player, accepted);
    }

    public void handleXValueChosen(Player player, int chosenValue) {
        gameService.handleXValueChosen(gameData, player, chosenValue);
    }

    public void paySearchTax(Player player) {
        gameService.paySearchTax(gameData, player);
    }

    public void handleCombatDamageAssigned(Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        gameService.handleCombatDamageAssigned(gameData, player, attackerIndex, assignments);
    }

    /**
     * Ensures the given player has priority without changing whose turn it is.
     * If the player is the active player, clears their pass. If not, marks the
     * active player as having passed so priority falls through to this player.
     * Called automatically by harness action methods (cast, activate, tap) so
     * tests don't need to manually set up priority state.
     */
    public void ensurePriority(Player player) {
        if (gameData.activePlayerId == null) {
            gameData.activePlayerId = player.getId();
        }
        if (player.getId().equals(gameData.activePlayerId)) {
            gameData.priorityPassedBy.remove(player.getId());
        } else {
            gameData.priorityPassedBy.add(gameData.activePlayerId);
            gameData.priorityPassedBy.remove(player.getId());
        }
        gameData.interaction.clearAwaitingInput();
    }

    public void forceActivePlayer(Player player) {
        gameData.activePlayerId = player.getId();
        gameData.startingPlayerId = player.getId();
    }

    public void forceStep(TurnStep step) {
        gameData.currentStep = step;
    }

    public void clearPriorityPassed() {
        gameData.priorityPassedBy.clear();
    }

    public void assertLife(Player player, int expectedLife) {
        assertThat(gameData.playerLifeTotals.get(player.getId())).isEqualTo(expectedLife);
    }

    public void assertOnBattlefield(Player player, String cardName) {
        assertThat(gameData.playerBattlefields.get(player.getId()))
                .anyMatch(p -> p.getCard().getName().equals(cardName));
    }

    public void assertNotOnBattlefield(Player player, String cardName) {
        assertThat(gameData.playerBattlefields.get(player.getId()))
                .noneMatch(p -> p.getCard().getName().equals(cardName));
    }

    public void assertInGraveyard(Player player, String cardName) {
        assertThat(gameData.playerGraveyards.get(player.getId()))
                .anyMatch(c -> c.getName().equals(cardName));
    }

    public void assertNotInGraveyard(Player player, String cardName) {
        assertThat(gameData.playerGraveyards.get(player.getId()))
                .noneMatch(c -> c.getName().equals(cardName));
    }

    public void assertInHand(Player player, String cardName) {
        assertThat(gameData.playerHands.get(player.getId()))
                .anyMatch(c -> c.getName().equals(cardName));
    }

    public void assertNotInHand(Player player, String cardName) {
        assertThat(gameData.playerHands.get(player.getId()))
                .noneMatch(c -> c.getName().equals(cardName));
    }

    public GameData getGameData() {
        return gameData;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public FakeConnection getConn1() {
        return conn1;
    }

    public FakeConnection getConn2() {
        return conn2;
    }

    public GameService getGameService() {
        return gameService;
    }

    public GameRegistry getGameRegistry() {
        return gameRegistry;
    }

    public GameQueryService getGameQueryService() {
        return gameQueryService;
    }

    public TargetValidationService getTargetValidationService() {
        return targetValidationService;
    }

    public LegendRuleService getLegendRuleService() {
        return legendRuleService;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public WebSocketSessionManager getSessionManager() {
        return sessionManager;
    }

    public PermanentRemovalService getPermanentRemovalService() {
        return permanentRemovalService;
    }

    public StackResolutionService getStackResolutionService() {
        return stackResolutionService;
    }

    public DrawService getDrawService() {
        return drawService;
    }

    public LifeResolutionService getLifeResolutionService() {
        return lifeResolutionService;
    }

    public PlayerInputService getPlayerInputService() {
        return playerInputService;
    }

    public GameBroadcastService getGameBroadcastService() {
        return gameBroadcastService;
    }

    public BattlefieldEntryService getBattlefieldEntryService() {
        return battlefieldEntryService;
    }

    public TriggerCollectionService getTriggerCollectionService() {
        return triggerCollectionService;
    }

    public SpellCastingService getSpellCastingService() {
        return spellCastingService;
    }

    public CombatAttackService getCombatAttackService() {
        return combatAttackService;
    }

    public void clearMessages() {
        conn1.clearMessages();
        conn2.clearMessages();
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
                        && params[1] == StackEntry.class
                        && CardEffect.class.isAssignableFrom(params[2])) {
                    Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[2];
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry, effectParam.cast(effect)); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 2
                        && params[0] == GameData.class
                        && params[1] == StackEntry.class) {
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 1
                        && params[0] == GameData.class) {
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd); }
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

