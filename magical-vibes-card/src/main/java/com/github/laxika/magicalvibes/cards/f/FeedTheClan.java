package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "KTK", collectorNumber = "132")
public class FeedTheClan extends Card {

    public FeedTheClan() {
        var ferocious = new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(ferocious, new GainLifeEffect(10)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(ferocious), new GainLifeEffect(5)));
    }
}
