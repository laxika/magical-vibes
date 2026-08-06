package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "171")
public class HighPriestOfPenance extends Card {

    public HighPriestOfPenance() {
        // Whenever this creature is dealt damage, you may destroy target nonland permanent.
        // The target is chosen as the trigger goes on the stack; the "may" is resolved on
        // the stack (CR 603.3d).
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.ON_DEALT_DAMAGE,
                        new MayEffect(new DestroyTargetPermanentEffect(),
                                "destroy target nonland permanent?"));
    }
}
