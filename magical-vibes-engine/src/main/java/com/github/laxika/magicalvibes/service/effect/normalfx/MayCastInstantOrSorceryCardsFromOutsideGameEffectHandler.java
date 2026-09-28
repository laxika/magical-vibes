package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardFromOutsideGameWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsFromOutsideGameEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Queues exclusive normal-cost cast offers for instant and sorcery sideboard cards. */
@Component
public class MayCastInstantOrSorceryCardsFromOutsideGameEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastInstantOrSorceryCardsFromOutsideGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> cards = com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, controllerId).stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();
        UUID offerGroupId = UUID.randomUUID();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastCardFromOutsideGameWithNormalCostEffect(offerGroupId)),
                    "Cast " + card.getName() + " from outside the game?",
                    card.getId()));
        }
    }
}
