package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
abstract class AbstractPlayerInteractionHandlerTest {

    @Mock protected DrawService drawService;
    @Mock protected GraveyardService graveyardService;
    @Mock protected GameQueryService gameQueryService;
    @Mock protected PredicateEvaluationService predicateEvaluationService;
    @Mock protected GameBroadcastService gameBroadcastService;
    @Mock protected PlayerInputService playerInputService;
    @Mock protected SessionManager sessionManager;
    @Mock protected CardViewFactory cardViewFactory;
    @Mock protected PermanentRemovalService permanentRemovalService;
    @Mock protected BattlefieldEntryService battlefieldEntryService;
    @Mock protected TriggerCollectionService triggerCollectionService;
    @Mock protected InteractionHandlerRegistry interactionHandlerRegistry;

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

        support = new PlayerInteractionSupport(drawService, graveyardService, gameQueryService, predicateEvaluationService,
                gameBroadcastService, playerInputService, sessionManager, cardViewFactory,
                permanentRemovalService, battlefieldEntryService, triggerCollectionService, interactionHandlerRegistry);
        registry = new EffectHandlerRegistry();
        String handlerName = getClass().getSimpleName().replace("Test", "");
        NormalEffectHandlerBean handler = PlayerInteractionHandlerTestSupport.createHandler(
                handlerName, support, registry, gameBroadcastService, drawService, sessionManager, cardViewFactory,
                gameQueryService, predicateEvaluationService, playerInputService, triggerCollectionService, battlefieldEntryService,
                permanentRemovalService, graveyardService, interactionHandlerRegistry);
        PlayerInteractionHandlerTestSupport.registerHandler(registry, handler);
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
