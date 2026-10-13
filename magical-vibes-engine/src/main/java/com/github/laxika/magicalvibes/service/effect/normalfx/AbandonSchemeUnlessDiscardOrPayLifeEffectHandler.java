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
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AbandonSchemeUnlessDiscardOrPayLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PermanentRemovalService permanentRemovalService;

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

        if (!canDiscard && !canPayLife) {
            abandon(gameData, source);
            return;
        }

        String choice = canDiscard && canPayLife
                ? "Discard a card? If you don't, pay " + e.lifeCost() + " life."
                : canDiscard
                ? "Discard a card? If you don't, abandon this scheme."
                : "Pay " + e.lifeCost() + " life? If you don't, abandon this scheme.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(e),
                choice + " (" + entry.getCard().getName() + ")",
                null, null, entry.getSourcePermanentId()));
    }

    public boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return amount >= 0
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= amount;
    }

    public void discard(GameData gameData, UUID playerId) {
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
    }

    /** CR 701.33b: the scheme is turned face down and put on the bottom of its owner's scheme deck. */
    public void abandon(GameData gameData, Permanent scheme) {
        Card card = scheme.getCard();
        if (permanentRemovalService.removePermanentToCommandZone(gameData, scheme)) {
            gameData.faceDownCommandZoneCards.add(card.getId());
        }
    }
}
