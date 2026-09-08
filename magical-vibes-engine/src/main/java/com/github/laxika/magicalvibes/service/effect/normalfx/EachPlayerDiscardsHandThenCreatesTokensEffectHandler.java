package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenCreatesTokensEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the per-player discard count and token creation of Awaken the Erstwhile. Each player
 * completes both actions in APNAP order, and their own discard count determines their tokens.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsHandThenCreatesTokensEffectHandler implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsHandThenCreatesTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerDiscardsHandThenCreatesTokensEffect) effect;
        String cardName = entry.getCard().getName();

        for (UUID playerId : apnapOrder(gameData)) {
            int discardCount = discardHandEffectHandler.discardHand(
                    gameData, playerId, entry.getControllerId(), cardName);
            if (discardCount == 0) {
                continue;
            }

            CreateTokenEffect tokens = e.token().withAmount(discardCount);
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, playerId, tokens, entry.getCard().getSetCode()));
        }
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            order.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
