package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "283")
@CardRegistration(set = "MB2", collectorNumber = "519")
public class MuragandaEldrazi extends Card {

    public MuragandaEldrazi() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastThisSpell(),
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PRIMEVAL, 1, new PermanentIsCreaturePredicate()));
    }
}
