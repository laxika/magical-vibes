package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerControlledMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "170")
public class BountifulHarvest extends Card {

    public BountifulHarvest() {
        addEffect(EffectSlot.SPELL, new GainLifePerControlledMatchingPermanentEffect(
                List.of(new PermanentIsLandPredicate())
        ));
    }
}
