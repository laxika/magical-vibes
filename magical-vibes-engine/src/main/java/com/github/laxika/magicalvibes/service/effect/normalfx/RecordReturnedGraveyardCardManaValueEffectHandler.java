package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardManaValueEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RecordReturnedGraveyardCardManaValueEffect} for Vengeful Rebirth: sets the entry's
 * event value to the returned graveyard card's mana value when a nonland card was actually returned
 * to the controller's hand, else 0. The following {@code DealDamageToAnyTargetEffect(new EventValue())}
 * reads it (0 deals no damage).
 */
@Component
public class RecordReturnedGraveyardCardManaValueEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RecordReturnedGraveyardCardManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);

        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null) {
            return;
        }

        // "If you return a nonland card to your hand this way": confirm the graveyard card was
        // actually returned (it is now in the controller's hand) rather than inferring return from
        // its type — a graveyard target that became illegal is never returned, so no damage.
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        Card returnedCard = hand == null ? null
                : hand.stream().filter(c -> c.getId().equals(returnedCardId)).findFirst().orElse(null);
        if (returnedCard == null || returnedCard.hasType(CardType.LAND)) {
            return;
        }

        entry.setEventValue(returnedCard.getManaValue());
    }
}
