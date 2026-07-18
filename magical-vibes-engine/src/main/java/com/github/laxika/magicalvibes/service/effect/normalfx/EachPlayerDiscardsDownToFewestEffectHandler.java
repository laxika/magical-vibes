package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsDownToFewestEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsDownToFewestEffect}: every player discards cards down to the
 * number held by the player with the fewest cards in hand. In APNAP order (active player first) each
 * player discards {@code handSize - fewest} cards of their own choice. Players already at the
 * minimum discard nothing. Rides the shared per-player discard queue via a variable-amount
 * {@link DiscardFollowUp} so each player's discard resolves sequentially before the spell's
 * remaining effects continue (Balance).
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsDownToFewestEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsDownToFewestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID activePlayerId = gameData.activePlayerId;
        List<UUID> ordered = orderedApnap(gameData, activePlayerId);

        int fewest = Integer.MAX_VALUE;
        for (UUID playerId : ordered) {
            fewest = Math.min(fewest, handSize(gameData, playerId));
        }
        if (fewest == Integer.MAX_VALUE) {
            fewest = 0;
        }

        List<UUID> choosers = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();
        for (UUID playerId : ordered) {
            int amount = handSize(gameData, playerId) - fewest;
            if (amount > 0) {
                choosers.add(playerId);
                amounts.add(amount);
            }
        }

        if (choosers.isEmpty()) {
            return;
        }

        playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                DiscardFollowUp.eachPlayerVariableAmounts(choosers, controllerId, amounts));
    }

    private int handSize(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand == null ? 0 : hand.size();
    }

    /** Active player first, then every other player in seating order (CR 101.4 APNAP). */
    private List<UUID> orderedApnap(GameData gameData, UUID activePlayerId) {
        List<UUID> ordered = new ArrayList<>();
        if (gameData.orderedPlayerIds.contains(activePlayerId)) {
            ordered.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                ordered.add(playerId);
            }
        }
        return ordered;
    }
}
