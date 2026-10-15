package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "BLB", collectorNumber = "30")
public class SeasonedWarrenguard extends Card {

    public SeasonedWarrenguard() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new ControlsPermanent(new PermanentIsTokenPredicate()), new BoostSelfEffect(2, 0)));
    }
}
