package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeUnlessDiscardOrPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AbandonSchemeUnlessDiscardOrPayLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AbandonSchemeUnlessDiscardOrPayLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AbandonSchemeUnlessDiscardOrPayLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean canDiscard = hand != null && !hand.isEmpty();
        boolean canPayLife = canPayLife(gameData, controllerId, e.lifeCost());

        if (canDiscard && canPayLife) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId, List.of(e),
                    "Discard a card? If you don't, pay " + e.lifeCost() + " life. ("
                            + entry.getCard().getName() + ")",
                    null, null, entry.getSourcePermanentId()));
        } else if (canDiscard) {
            discard(gameData, controllerId);
        } else if (canPayLife) {
            lifeSupport.applyLifePayment(gameData, controllerId, e.lifeCost(), entry.getCard().getName());
        } else {
            destructionSupport.sacrificeAndLog(gameData, source, controllerId);
        }
    }

    private boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return amount >= 0
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= amount;
    }

    private void discard(GameData gameData, UUID playerId) {
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
    }
}
