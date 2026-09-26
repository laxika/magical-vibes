package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellWhenCounteredOrFizzlesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "MB1", collectorNumber = "68")
public class TrialAndError extends Card {

    public TrialAndError() {
        addEffect(EffectSlot.ON_SELF_SPELL_COUNTERED_OR_FIZZLED,
                new CopyThisSpellWhenCounteredOrFizzlesEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
