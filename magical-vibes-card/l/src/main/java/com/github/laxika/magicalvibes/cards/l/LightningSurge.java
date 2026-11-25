package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "JUD", collectorNumber = "96")
public class LightningSurge extends Card {

    public LightningSurge() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addSpellEffect(new ConditionalReplacementEffect(
                threshold,
                DealDamageEffect.toAnyTarget(4),
                DealDamageEffect.toAnyTarget(new Fixed(6)).unpreventableWhen(threshold)
        ));
        addCastingOption(new FlashbackCast("{5}{R}{R}"));
    }
}
