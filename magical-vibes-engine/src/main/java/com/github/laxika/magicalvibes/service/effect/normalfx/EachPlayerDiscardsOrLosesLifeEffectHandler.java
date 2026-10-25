package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOrLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves the APNAP discard-or-life-loss flow used by Liliana, Waker of the Dead. Each player with
 * a card in hand chooses one in APNAP order (CR 101.4); the chosen cards are discarded together once
 * every choice is made. A player with an empty hand loses life when their turn in the order comes.
 *
 * <p>Selections use the deferred {@link EachPlayerRummageState} discard flow shared with the other
 * each-player discard effects.</p>
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: each run changes nothing before its losses (a selection is only
 * recorded), and a discard prompt ends it, so before anything changes the run asks, through
 * {@link LifeSupport#askBeforeLifeLosses}, the Lich's Duel Mastery sacrifices (CR 616.1) of the losses it reaches
 * before the next prompt (the first loss closes the window).</p>
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsOrLosesLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final ObjectProvider<CardChoiceHandlerService> cardChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsOrLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsOrLosesLifeEffect discardEffect = (EachPlayerDiscardsOrLosesLifeEffect) effect;
        EachPlayerRummageState state = gameData.eachPlayerRummage;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.deferDiscards = true;
            addInApnapOrder(gameData, state.remaining);
        }

        beginNextPlayer(gameData, entry, discardEffect);
    }

    private void beginNextPlayer(GameData gameData, StackEntry entry, EachPlayerDiscardsOrLosesLifeEffect effect) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        UUID controllerId = entry.getControllerId();

        Map<UUID, Integer> lossesBeforeNextPrompt = new LinkedHashMap<>();
        for (UUID playerId : state.remaining) {
            List<Card> hand = gameData.playerHands.get(playerId);
            boolean emptyHand = hand == null || hand.isEmpty();
            if (emptyHand && !playerId.equals(controllerId)) {
                lossesBeforeNextPrompt.put(playerId, effect.lifeLoss());
            } else if (!emptyHand && (playerId.equals(controllerId)
                    || !gameQueryService.isDiscardPrevented(gameData, playerId))) {
                break;
            }
        }
        lifeSupport.askBeforeLifeLosses(gameData, lossesBeforeNextPrompt);

        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.pollFirst();
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                if (!playerId.equals(controllerId)) {
                    lifeSupport.applyLifeLoss(gameData, playerId, effect.lifeLoss(), entry.getCard().getName());
                }
                continue;
            }

            state.currentPlayerId = playerId;
            gameData.discardCausedByOpponent = !playerId.equals(controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            state.currentPlayerId = null;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        List<EachPlayerRummageState.SelectedDiscard> selected = List.copyOf(state.selectedDiscards);
        state.reset();
        cardChoiceHandlerServiceProvider.getObject().discardCollectedCards(gameData, selected, controllerId);
    }

    private void addInApnapOrder(GameData gameData, java.util.Deque<UUID> order) {
        if (gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            order.addLast(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.addLast(playerId);
            }
        }
    }
}
