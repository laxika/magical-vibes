package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCreatureEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscardTwoUnlessCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardTwoUnlessCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean hasCreature = hand != null && hand.stream().anyMatch(card -> card.hasType(CardType.CREATURE));

        if (!hasCreature) {
            gameData.discardCausedByOpponent = false;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 2);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(effect),
                "Discard a creature card instead of discarding two? (" + entry.getCard().getName() + ")"
        ));
    }
}
