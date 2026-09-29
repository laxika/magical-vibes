package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/** Resolves Skullpiercer Gnat's random perpetual hand-card trigger. */
@Component
public class PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.get(entry.getTargetId());
        if (hand == null) {
            return;
        }

        List<Integer> nonlandIndices = IntStream.range(0, hand.size())
                .filter(index -> !hand.get(index).hasType(CardType.LAND))
                .boxed()
                .toList();
        if (nonlandIndices.isEmpty()) {
            return;
        }

        int handIndex = nonlandIndices.get(ThreadLocalRandom.current().nextInt(nonlandIndices.size()));
        Card modifiedCard = hand.get(handIndex).createRuntimeCopy();
        modifiedCard.addEffect(EffectSlot.ON_SELF_CAST,
                new GivePoisonCountersEffect(1, PoisonRecipient.CONTROLLER));
        modifiedCard.freeze();
        hand.set(handIndex, modifiedCard);
    }
}
