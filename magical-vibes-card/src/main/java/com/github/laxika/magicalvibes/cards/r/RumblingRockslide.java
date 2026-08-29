package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "163")
public class RumblingRockslide extends Card {

    public RumblingRockslide() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureEffect(new PermanentCount(
                        new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
