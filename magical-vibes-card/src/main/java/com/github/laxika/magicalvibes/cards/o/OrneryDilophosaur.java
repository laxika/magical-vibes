package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "M21", collectorNumber = "194")
public class OrneryDilophosaur extends Card {

    public OrneryDilophosaur() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)),
                new BoostSelfEffect(2, 2)));
    }
}
