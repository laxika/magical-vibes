package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "2")
public class HaazdaSnareSquad extends Card {

    public HaazdaSnareSquad() {
        // Whenever this creature attacks, you may pay {W}. If you do, tap target creature an
        // opponent controls. Target is chosen as the trigger goes on the stack (CR 603.3d);
        // the payment choice happens at resolution (CR 603.5).
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{W}",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        "Pay {W} to tap target creature?"));
    }
}
