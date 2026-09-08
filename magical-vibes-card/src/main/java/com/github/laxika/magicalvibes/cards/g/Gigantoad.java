package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "FIN", collectorNumber = "187")
public class Gigantoad extends Card {

    public Gigantoad() {
        // As long as you control seven or more lands, this creature gets +2/+2.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(7, new PermanentIsLandPredicate()),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
