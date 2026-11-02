package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "47")
@CardRegistration(set = "GRN", collectorNumber = "31")
@CardRegistration(set = "IKO", collectorNumber = "44")
@CardRegistration(set = "2X2", collectorNumber = "42")
public class CaptureSphere extends Card {

    public CaptureSphere() {
        target(TargetFilters.creature());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.ENCHANTED));
        addStaticEffect(DoesntUntapEffect.enchanted());
    }
}
