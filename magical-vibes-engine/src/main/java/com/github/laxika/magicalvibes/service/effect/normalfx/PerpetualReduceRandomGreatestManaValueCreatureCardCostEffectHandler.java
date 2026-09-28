package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PerpetualReduceRandomGreatestManaValueCreatureCardCostEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reduction = (PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        int greatestManaValue = hand.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .mapToInt(Card::getManaValue)
                .max()
                .orElse(-1);
        if (greatestManaValue < 0) {
            return;
        }

        List<Card> candidates = hand.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> card.getManaValue() == greatestManaValue)
                .toList();
        Card chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        gameData.perpetualCardCastCostReductions.merge(
                chosen.getId(), reduction.amount(), Integer::sum);
    }
}
