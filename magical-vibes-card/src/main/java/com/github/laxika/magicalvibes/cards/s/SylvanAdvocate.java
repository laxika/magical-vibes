package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "OGW", collectorNumber = "144")
public class SylvanAdvocate extends Card {

    public SylvanAdvocate() {
        ControlsPermanentCount sixLands = new ControlsPermanentCount(6, new PermanentIsLandPredicate());

        addEffect(EffectSlot.STATIC, new ConditionalEffect(sixLands,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(sixLands,
                new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES, new PermanentIsLandPredicate())));
    }
}
