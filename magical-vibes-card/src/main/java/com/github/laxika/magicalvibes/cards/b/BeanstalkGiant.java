package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ELD", collectorNumber = "149")
public class BeanstalkGiant extends Card {

    public BeanstalkGiant() {
        setBackFaceCard(new FertileFootsteps());
        addCastingOption(new AdventureCast("{2}{G}"));

        PermanentCount lands = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(lands, lands));
    }

    @Override
    public String getBackFaceClassName() {
        return "FertileFootsteps";
    }
}
