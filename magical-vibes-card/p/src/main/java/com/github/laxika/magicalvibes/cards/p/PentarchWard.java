package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "33")
@CardRegistration(set = "MB1", collectorNumber = "199")
@CardRegistration(set = "IMA", collectorNumber = "27")
public class PentarchWard extends Card {

    public PentarchWard() {
        target(TargetFilters.creature())
                .addStaticEffect(new ChooseColorOnEnterEffect())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1))
                .addStaticEffect(new ProtectionFromChosenColorEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
