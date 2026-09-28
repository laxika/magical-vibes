package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsDownToHandSizeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an opponent-only discard whose amount is computed from each opponent's hand. */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsDownToHandSizeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsDownToHandSizeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentDiscardsDownToHandSizeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> choosers = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();

        for (UUID playerId : orderedOpponents(gameData, controllerId)) {
            if (!gameQueryService.canEffectCauseDiscard(gameData, playerId, controllerId)) {
                continue;
            }
            List<Card> hand = gameData.playerHands.get(playerId);
            int handSize = hand == null ? 0 : hand.size();
            int amount = Math.max(0, handSize - e.handSize());
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

    /** Active player first, then the remaining opponents in seating order. */
    private List<UUID> orderedOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (gameData.orderedPlayerIds.contains(activePlayerId)
                && !activePlayerId.equals(controllerId)) {
            ordered.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                ordered.add(playerId);
            }
        }
        return ordered;
    }
}
