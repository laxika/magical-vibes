package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetCreatureDealsPowerDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "63")
public class RavenousGigantotherium extends Card {

    public RavenousGigantotherium() {
        addStaticEffect(new DevourEffect(3));

        targetUpTo(new SourcePower(), TargetFilters.creature(), 99)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new DealDividedDamageEffect(new SourcePower(), null, DivisionMode.CHOSEN,
                                new PermanentIsCreaturePredicate(), 0, false, false, true),
                        new EachTargetCreatureDealsPowerDamageToSourceEffect());
    }
}
