package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "17")
@CardRegistration(set = "OPCA", collectorNumber = "38")
public class Immersturm extends Card {

    public Immersturm() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new MayEffect(DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower()),
                        "Have it deal damage to any target?", null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                FlickerEffect.flickerTarget(new PermanentIsCreaturePredicate()));
    }
}
