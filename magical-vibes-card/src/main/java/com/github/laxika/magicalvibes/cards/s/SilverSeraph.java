package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "JUD", collectorNumber = "23")
public class SilverSeraph extends Card {

    public SilverSeraph() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES)));
    }
}
