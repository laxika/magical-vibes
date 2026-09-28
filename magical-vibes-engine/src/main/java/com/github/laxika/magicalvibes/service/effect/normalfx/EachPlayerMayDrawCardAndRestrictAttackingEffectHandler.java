package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawCardAndRestrictAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves The Second Doctor's independent draw choices in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDrawCardAndRestrictAttackingEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDrawCardAndRestrictAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.pendingEachPlayerDrawUpToInitialCount != null) {
            finishCurrentPlayer(gameData, entry);
            return;
        }

        if (gameData.chosenXValue != null) {
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.getFirst();
            gameData.pendingEachPlayerDrawUpToInitialCount =
                    gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
            gameData.pendingEachPlayerMayDrawChosenCount = chosen;
            if (chosen > 0) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, chosen);
            }
            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                return;
            }
            finishCurrentPlayer(gameData, entry);
            return;
        }

        gameData.pendingEachPlayerDrawUpToQueue.clear();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            gameData.pendingEachPlayerDrawUpToQueue.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerDrawUpToQueue.add(playerId);
            }
        }
        promptNextPlayer(gameData, entry);
    }

    private void promptNextPlayer(GameData gameData, StackEntry entry) {
        if (gameData.pendingEachPlayerDrawUpToQueue.isEmpty()) {
            return;
        }
        String cardName = entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                gameData.pendingEachPlayerDrawUpToQueue.getFirst(), 1,
                "Draw a card for " + cardName + "?", cardName));
    }

    private void finishCurrentPlayer(GameData gameData, StackEntry entry) {
        UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.removeFirst();
        boolean drew = gameData.pendingEachPlayerMayDrawChosenCount != null
                && gameData.pendingEachPlayerDrawUpToInitialCount != null
                && gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0)
                > gameData.pendingEachPlayerDrawUpToInitialCount;
        gameData.pendingEachPlayerDrawUpToInitialCount = null;
        gameData.pendingEachPlayerMayDrawChosenCount = null;

        if (drew && !playerId.equals(entry.getControllerId())) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(), playerId,
                    new CreaturesCantAttackControllerUnlessPredicateEffect(
                            new PermanentNotPredicate(new PermanentTruePredicate()), false, playerId, true),
                    null, entry.getControllerId(), null, EffectDuration.UNTIL_END_OF_YOUR_NEXT_TURN, 0L));
        }
        promptNextPlayer(gameData, entry);
    }
}
