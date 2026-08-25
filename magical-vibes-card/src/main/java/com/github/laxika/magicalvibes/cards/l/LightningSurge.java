package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "JUD", collectorNumber = "96")
public class LightningSurge extends Card {

    public LightningSurge() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                threshold,
                new DealDamageToAnyTargetEffect(4),
                new DealDamageToAnyTargetEffect(new Fixed(6), threshold)
        ));
        addCastingOption(new FlashbackCast("{5}{R}{R}"));
    }
}
