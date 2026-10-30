package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves an opponent-by-opponent discard-or-life-loss effect in APNAP order: an opponent with a card in hand is
 * asked, one without loses the life at once.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: before anything changes, the Lich's Duel Mastery sacrifices the
 * at-once losses would cause are asked about (CR 616.1) through
 * {@link LifeSupport#askBeforeLifeLossesThatFollowAChange}, since an earlier opponent's loss closes the window before
 * a later opponent's.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentMayDiscardOrLoseLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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

        List<UUID> opponents = apnapOpponents(gameData, controllerId);
        Map<UUID, Integer> lossesAtOnce = new LinkedHashMap<>();
        for (UUID opponentId : opponents) {
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()) {
                lossesAtOnce.put(opponentId, discardEffect.lifeLoss());
            }
        }
        lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, lossesAtOnce);

        for (UUID opponentId : opponents) {
            if (lossesAtOnce.containsKey(opponentId)) {
                lifeSupport.applyLifeLoss(gameData, opponentId, discardEffect.lifeLoss(),
                        entry.getCard().getName());
                continue;
            }

            String prompt = "Discard a card? If you don't, you lose " + discardEffect.lifeLoss()
                    + " life. (" + entry.getCard().getName() + ")";
            gameData.pendingMayAbilities.addLast(new PendingMayAbility(
                    entry.getCard(), opponentId,
                    List.of(new LoseLifeUnlessDiscardEffect(discardEffect.lifeLoss())), prompt,
                    null, controllerId));
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
