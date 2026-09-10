package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OHOP", collectorNumber = "17")
public class Immersturm extends Card {

    public Immersturm() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(DealDamageToAnyTargetEffect.fromEnteringPermanent(new SourcePower()),
                        "Have it deal damage to any target?", null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                FlickerEffect.flickerTarget(new PermanentIsCreaturePredicate()));
    }
}
