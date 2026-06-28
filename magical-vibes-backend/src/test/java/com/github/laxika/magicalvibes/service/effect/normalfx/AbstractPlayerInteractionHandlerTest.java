package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
abstract class AbstractPlayerInteractionHandlerTest {

    @Mock protected DrawService drawService;
    @Mock protected GraveyardService graveyardService;
    @Mock protected GameQueryService gameQueryService;
    @Mock protected GameBroadcastService gameBroadcastService;
    @Mock protected PlayerInputService playerInputService;
    @Mock protected SessionManager sessionManager;
    @Mock protected CardViewFactory cardViewFactory;
    @Mock protected PermanentRemovalService permanentRemovalService;
    @Mock protected BattlefieldEntryService battlefieldEntryService;
    @Mock protected TriggerCollectionService triggerCollectionService;

    protected EffectHandlerRegistry registry;
    protected PlayerInteractionSupport support;
    protected GameData gd;
    protected UUID player1Id;
    protected UUID player2Id;

    @BeforeEach
    void setUpPlayerInteractionHandlerBase() {
        var game = EffectHandlerTestFixtures.newTwoPlayerGameDataFull();
        player1Id = game.player1Id();
        player2Id = game.player2Id();
        gd = game.gameData();

        support = new PlayerInteractionSupport(drawService, graveyardService, gameQueryService,
                gameBroadcastService, playerInputService, sessionManager, cardViewFactory,
                permanentRemovalService, battlefieldEntryService, triggerCollectionService);
        registry = new EffectHandlerRegistry();
        LifeSupport lifeSupport = new LifeSupport(gameQueryService, gameBroadcastService, triggerCollectionService);
        TapUntapSupport tapUntapSupport = new TapUntapSupport(triggerCollectionService);
        CreatureControlService creatureControlService = mock(CreatureControlService.class);
        AnimationSupport animationSupport = new AnimationSupport(
                gameQueryService, gameBroadcastService, playerInputService, creatureControlService);
        DamagePreventionService damagePreventionService = mock(DamagePreventionService.class);
        GameOutcomeService gameOutcomeService = mock(GameOutcomeService.class);
        DamageSupport damageSupport = new DamageSupport(
                graveyardService, damagePreventionService, gameOutcomeService, gameQueryService,
                gameBroadcastService, permanentRemovalService, triggerCollectionService, lifeSupport);
        LegendRuleService legendRuleService = mock(LegendRuleService.class);
        DestructionSupport destructionSupport = new DestructionSupport(
                battlefieldEntryService, graveyardService, damagePreventionService, gameOutcomeService,
                permanentRemovalService, gameQueryService, gameBroadcastService, playerInputService, lifeSupport);
        ExileService exileService = mock(ExileService.class);
        GraveyardReturnSupport graveyardReturnSupport = new GraveyardReturnSupport(
                battlefieldEntryService, permanentRemovalService, legendRuleService, gameQueryService,
                gameBroadcastService, playerInputService, lifeSupport, exileService, cardViewFactory);
        LibraryRevealSupport libraryRevealSupport = new LibraryRevealSupport(
                gameBroadcastService, sessionManager, cardViewFactory);
        LibrarySearchSupport librarySearchSupport = new LibrarySearchSupport(
                gameBroadcastService, sessionManager, cardViewFactory);
        ExileSupport exileSupport = new ExileSupport(
                graveyardService, gameQueryService, gameBroadcastService, permanentRemovalService,
                playerInputService, triggerCollectionService);
        PermanentControlSupport permanentControlSupport = new PermanentControlSupport(
                battlefieldEntryService, legendRuleService, gameQueryService, gameBroadcastService);
        PermanentCounterSupport permanentCounterSupport = new PermanentCounterSupport(
                gameQueryService, gameBroadcastService, playerInputService);
        CardSpecificSupport cardSpecificSupport = new CardSpecificSupport();
        WarpWorldService warpWorldService = mock(WarpWorldService.class);
        StateTriggerService stateTriggerService = mock(StateTriggerService.class);
        ValidTargetService validTargetService = mock(ValidTargetService.class);
        CloneService cloneService = mock(CloneService.class);
        CombatService combatService = mock(CombatService.class);
        AuraAttachmentService auraAttachmentService = mock(AuraAttachmentService.class);
        TurnCleanupService turnCleanupService = new TurnCleanupService(auraAttachmentService);
        TargetLegalityService targetLegalityService = mock(TargetLegalityService.class);
        NormalEffectHandlerBeanFactory.registerAll(
                NormalEffectHandlerBeanFactory.createAll(
                        lifeSupport, tapUntapSupport, animationSupport, damageSupport, destructionSupport,
                        graveyardReturnSupport, libraryRevealSupport, librarySearchSupport, exileSupport,
                        permanentControlSupport, permanentCounterSupport, battlefieldEntryService, legendRuleService,
                        creatureControlService, gameQueryService, gameBroadcastService, gameOutcomeService,
                        graveyardService, exileService, permanentRemovalService, triggerCollectionService,
                        playerInputService, drawService, sessionManager, cardViewFactory, cardSpecificSupport,
                        warpWorldService, registry, stateTriggerService, validTargetService, cloneService,
                        combatService, auraAttachmentService, turnCleanupService, targetLegalityService),
                registry);
        setUpHandler();
    }

    protected void setUpHandler() {
    }

    protected void resolveEffect(GameData gameData, StackEntry entry, CardEffect effect) {
        EffectHandler handler = registry.getHandler(effect);
        handler.resolve(gameData, entry, effect);
    }

    protected Card createCard(String name) {
        return EffectHandlerTestFixtures.createCard(name);
    }

    protected StackEntry createEntry(Card card, UUID controllerId, List<CardEffect> effects) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), effects);
    }

    protected StackEntry createEntryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                effects, 0, targetId, null);
    }

    protected StackEntry createEntryWithXValue(Card card, UUID controllerId, List<CardEffect> effects, int xValue) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), effects, xValue);
    }

    protected StackEntry createEntryWithXValueAndTarget(Card card, UUID controllerId, List<CardEffect> effects, int xValue, UUID targetId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(),
                effects, xValue, targetId, null);
    }

    protected StackEntry createTriggeredEntry(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId, card.getName(), effects, null, sourcePermanentId);
    }

    protected StackEntry createTriggeredEntryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId, card.getName(),
                effects, 0, targetId, sourcePermanentId, null, null, null, null);
    }
}
