package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostRandomCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/** Applies a perpetual +X/+X modification to a random creature card in hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostRandomCreatureCardInHandEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostRandomCreatureCardInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostRandomCreatureCardInHandEffect boost =
                (PerpetuallyBoostRandomCreatureCardInHandEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        List<Integer> creatureIndices = IntStream.range(0, hand.size())
                .filter(index -> hand.get(index).hasType(CardType.CREATURE))
                .boxed()
                .toList();
        if (creatureIndices.isEmpty()) {
            return;
        }

        int handIndex = creatureIndices.get(ThreadLocalRandom.current().nextInt(creatureIndices.size()));
        Card copy = hand.get(handIndex).createRuntimeCopy();
        int amount = amountEvaluationService.evaluate(gameData, boost.powerBoost(),
                AmountContext.forStackEntry(entry, null));
        if (copy.getPower() != null) {
            copy.setPower(copy.getPower() + amount);
        }
        if (copy.getToughness() != null) {
            copy.setToughness(copy.getToughness() + amount);
        }
        copy.freeze();
        hand.set(handIndex, copy);
    }
}
