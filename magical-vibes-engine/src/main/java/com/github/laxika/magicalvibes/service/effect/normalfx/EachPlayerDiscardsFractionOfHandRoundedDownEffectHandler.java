package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsFractionOfHandRoundedDownEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves the per-player rounded-down discard fraction in APNAP order. The amount is computed
 * from each player's current hand and the shared variable-amount discard queue continues the
 * spell after all choices are complete.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsFractionOfHandRoundedDownEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsFractionOfHandRoundedDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerDiscardsFractionOfHandRoundedDownEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> choosers = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();
        for (UUID playerId : orderedApnap(gameData, gameData.activePlayerId)) {
            List<Card> hand = gameData.playerHands.get(playerId);
            int handSize = hand == null ? 0 : hand.size();
            int amount = handSize / e.divisor();
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
