package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "607")
public class QuantumEntanglement extends Card {

    public QuantumEntanglement() {
        SpellTarget creatureTarget = target(TargetFilters.creatureYouControl());
        creatureTarget.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, mayFlicker());
        creatureTarget.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, mayFlicker());
    }

    private static MayPayManaEffect mayFlicker() {
        return new MayPayManaEffect("{1}{W}", FlickerEffect.flickerTarget(),
                "Pay {1}{W} to exile target creature you control, then return it?");
    }
}
