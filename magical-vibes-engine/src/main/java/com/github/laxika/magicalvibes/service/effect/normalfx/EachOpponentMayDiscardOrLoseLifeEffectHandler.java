package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an opponent-by-opponent discard-or-life-loss effect in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayDiscardOrLoseLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayDiscardOrLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentMayDiscardOrLoseLifeEffect discardEffect =
                (EachOpponentMayDiscardOrLoseLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        for (UUID opponentId : apnapOpponents(gameData, controllerId)) {
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()) {
                lifeSupport.applyLifeLoss(gameData, opponentId, discardEffect.lifeLoss(),
                        entry.getCard().getName());
                continue;
            }

            String prompt = "Discard a card? If you don't, you lose " + discardEffect.lifeLoss()
                    + " life. (" + entry.getCard().getName() + ")";
            gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                    entry.getCard(), opponentId,
                    List.of(new LoseLifeUnlessDiscardEffect(discardEffect.lifeLoss())), prompt));
        }
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
