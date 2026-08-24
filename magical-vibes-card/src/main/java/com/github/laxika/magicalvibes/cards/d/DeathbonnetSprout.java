package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MID", collectorNumber = "181")
public class DeathbonnetSprout extends Card {

    public DeathbonnetSprout() {
        setBackFaceCard(new DeathbonnetHulk());

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                SequenceEffect.of(
                        new MillEffect(1, MillRecipient.CONTROLLER),
                        new ConditionalEffect(
                                new GraveyardCardThreshold(3, new CardTypePredicate(CardType.CREATURE)),
                                new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "DeathbonnetHulk";
    }
}
