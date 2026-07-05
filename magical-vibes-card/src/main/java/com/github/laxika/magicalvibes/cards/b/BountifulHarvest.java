package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "M10", collectorNumber = "170")
public class BountifulHarvest extends Card {

    public BountifulHarvest() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
