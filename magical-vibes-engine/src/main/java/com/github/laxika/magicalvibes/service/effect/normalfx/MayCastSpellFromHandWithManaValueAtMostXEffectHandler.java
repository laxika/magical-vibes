package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastSpellFromHandWithManaValueAtMostXEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Offers eligible hand spells through the shared free-cast choice flow.
 */
@Component
public class MayCastSpellFromHandWithManaValueAtMostXEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastSpellFromHandWithManaValueAtMostXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        int maxManaValue = entry.getXValue();
        List<Card> eligible = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> card.getManaValue() <= maxManaValue)
                .toList();

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }
}
