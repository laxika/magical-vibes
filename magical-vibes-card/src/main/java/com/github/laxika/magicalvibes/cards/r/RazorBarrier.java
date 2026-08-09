package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "17")
public class RazorBarrier extends Card {

    public RazorBarrier() {
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect(true));
    }
}
