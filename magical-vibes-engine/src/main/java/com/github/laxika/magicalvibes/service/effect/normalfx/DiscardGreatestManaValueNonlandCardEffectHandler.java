package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardGreatestManaValueNonlandCardEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted discard restricted to tied greatest-mana-value nonland cards. */
@Component
@RequiredArgsConstructor
public class DiscardGreatestManaValueNonlandCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardGreatestManaValueNonlandCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        int greatestManaValue = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .mapToInt(Card::getManaValue)
                .max()
                .orElse(Integer.MIN_VALUE);

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.LAND) && card.getManaValue() == greatestManaValue) {
                validIndices.add(i);
            }
        }

        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
        gameData.lastDiscardedCardManaValue = 0;
        gameData.greatestDiscardedCardManaValue = 0;
        gameData.lastDiscardedCardTypes = Set.of();
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, validIndices);
    }
}
