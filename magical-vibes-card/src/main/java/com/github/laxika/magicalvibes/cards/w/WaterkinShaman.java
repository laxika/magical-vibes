package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "M20", collectorNumber = "288")
public class WaterkinShaman extends Card {

    public WaterkinShaman() {
        // Whenever a creature you control with flying enters, this creature gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new BoostSelfEffect(1, 1)));
    }
}
