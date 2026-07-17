package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsFractionOfHandRoundedUpEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsFractionOfHandRoundedUpEffect}: in APNAP order (active player
 * first) each player discards {@code ceil(handSize / divisor)} cards of their own choice, where the
 * count is recomputed against that player's current hand. Players with an empty hand are skipped.
 * Rides the shared per-player discard queue via a variable-amount {@link DiscardFollowUp} so each
 * player's discard resolves sequentially before the spell's remaining effects continue (Pox).
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsFractionOfHandRoundedUpEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsFractionOfHandRoundedUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerDiscardsFractionOfHandRoundedUpEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID activePlayerId = gameData.activePlayerId;

        List<UUID> choosers = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();
        for (UUID playerId : orderedApnap(gameData, activePlayerId)) {
            List<Card> hand = gameData.playerHands.get(playerId);
            int handSize = hand == null ? 0 : hand.size();
            int amount = (handSize + e.divisor() - 1) / e.divisor();
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
