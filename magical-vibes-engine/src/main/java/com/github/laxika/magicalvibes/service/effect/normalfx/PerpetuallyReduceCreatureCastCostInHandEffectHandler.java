package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCreatureCastCostInHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Fountainport Charmer's perpetual creature-card cost reduction. */
@Component
public class PerpetuallyReduceCreatureCastCostInHandEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyReduceCreatureCastCostInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) {
            return;
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!card.hasType(CardType.CREATURE)) {
                continue;
            }

            Card modifiedCard = card.createRuntimeCopy();
            modifiedCard.addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new Fixed(1)));
            hand.set(i, modifiedCard);
        }
    }
}
