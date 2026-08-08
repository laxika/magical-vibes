package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardValueEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedGraveyardCardValue;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RecordReturnedGraveyardCardValueEffect}: sets the entry's event value to the
 * requested characteristic of the card actually returned to the controller's hand by the preceding
 * graveyard return, else 0. A following {@code DealDamageToAnyTargetEffect(new EventValue())} reads
 * it (0 deals no damage).
 */
@Component
public class RecordReturnedGraveyardCardValueEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RecordReturnedGraveyardCardValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);

        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null) {
            return;
        }

        // Confirm the graveyard card was actually returned (it is now in the controller's hand)
        // rather than inferring the return from its type — a graveyard target that became illegal
        // is never returned, so no damage.
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        Card returnedCard = hand == null ? null
                : hand.stream().filter(c -> c.getId().equals(returnedCardId)).findFirst().orElse(null);
        if (returnedCard == null) {
            return;
        }

        ReturnedGraveyardCardValue value = ((RecordReturnedGraveyardCardValueEffect) effect).value();
        if (value == ReturnedGraveyardCardValue.POWER) {
            entry.setEventValue(returnedCard.getPower() == null ? 0 : returnedCard.getPower());
            return;
        }

        // "If you return a nonland card to your hand this way": a returned land records 0.
        if (!returnedCard.hasType(CardType.LAND)) {
            entry.setEventValue(returnedCard.getManaValue());
        }
    }
}
