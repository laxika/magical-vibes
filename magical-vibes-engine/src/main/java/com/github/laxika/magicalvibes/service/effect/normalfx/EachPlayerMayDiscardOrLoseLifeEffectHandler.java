package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an APNAP discard-or-life-loss choice for every player. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDiscardOrLoseLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDiscardOrLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMayDiscardOrLoseLifeEffect discardEffect =
                (EachPlayerMayDiscardOrLoseLifeEffect) effect;

        for (UUID playerId : apnapPlayers(gameData)) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                lifeSupport.applyLifeLoss(gameData, playerId, discardEffect.lifeLoss(),
                        entry.getCard().getName());
                continue;
            }

            String prompt = "Discard a card? If you don't, you lose " + discardEffect.lifeLoss()
                    + " life. (" + entry.getCard().getName() + ")";
            gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                    entry.getCard(), playerId,
                    List.of(new LoseLifeUnlessDiscardEffect(discardEffect.lifeLoss())), prompt));
        }
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && gameData.orderedPlayerIds.contains(activePlayerId)) {
            players.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                players.add(playerId);
            }
        }
        return players;
    }
}
