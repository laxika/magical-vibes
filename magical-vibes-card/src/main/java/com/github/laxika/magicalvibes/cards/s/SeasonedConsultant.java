package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "MKM", collectorNumber = "33")
public class SeasonedConsultant extends Card {

    public SeasonedConsultant() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new MinimumAttackers(3), new BoostSelfEffect(2, 0)));
    }
}
