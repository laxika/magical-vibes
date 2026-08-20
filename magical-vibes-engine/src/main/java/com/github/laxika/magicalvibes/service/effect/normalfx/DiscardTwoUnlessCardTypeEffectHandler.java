package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCardTypeEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiscardTwoUnlessCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardTwoUnlessCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DiscardTwoUnlessCardTypeEffect discardEffect = (DiscardTwoUnlessCardTypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean hasMatchingCard = hand != null && hand.stream()
                .anyMatch(card -> discardEffect.cardTypes().stream().anyMatch(card::hasType));

        if (!hasMatchingCard) {
            gameData.discardCausedByOpponent = false;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 2);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(effect),
                "Discard a card of the required type instead of discarding two? ("
                        + entry.getCard().getName() + ")"
        ));
    }
}
