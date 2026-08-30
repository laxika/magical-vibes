package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachOpponentDiscardsTwoUnlessNonlandState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsTwoUnlessNonlandEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Bandit's Talent's APNAP discard choice one opponent at a time. */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsTwoUnlessNonlandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsTwoUnlessNonlandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentDiscardsTwoUnlessNonlandState state = gameData.eachOpponentDiscardsTwoUnlessNonland;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.remainingOpponentIds.addAll(apnapOpponents(gameData, entry.getControllerId()));
        }

        if (state.awaitingDiscard) {
            state.awaitingDiscard = false;
            state.currentOpponentId = null;
        }

        while (!state.remainingOpponentIds.isEmpty()) {
            UUID opponentId = state.remainingOpponentIds.removeFirst();
            state.currentOpponentId = opponentId;
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()) {
                state.currentOpponentId = null;
                continue;
            }

            boolean hasNonland = hand.stream().anyMatch(card -> !card.hasType(CardType.LAND));
            if (hasNonland) {
                state.awaitingMayChoice = true;
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        entry.getCard(), opponentId, List.of(effect),
                        "Discard a nonland card instead of discarding two? (" + entry.getCard().getName() + ")",
                        null, entry.getControllerId()));
                return;
            }

            beginDiscard(gameData, entry, state, opponentId, 2);
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void beginDiscard(GameData gameData, StackEntry entry,
            EachOpponentDiscardsTwoUnlessNonlandState state, UUID opponentId, int amount) {
        gameData.discardCausedByOpponent = !opponentId.equals(entry.getControllerId());
        state.awaitingMayChoice = false;
        state.awaitingDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInteractionSupport.resolveDiscardCards(gameData, opponentId, amount, DiscardFollowUp.NONE);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex >= 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream().filter(playerId -> !playerId.equals(controllerId)).toList();
    }
}
