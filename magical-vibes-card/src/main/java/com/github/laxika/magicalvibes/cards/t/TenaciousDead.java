package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "M14", collectorNumber = "118")
public class TenaciousDead extends Card {

    public TenaciousDead() {
        // When this creature dies, you may pay {1}{B}. If you do, return it to the battlefield
        // tapped under its owner's control.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{1}{B}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .enterTapped(true)
                        .underOwnersControl(true)
                        .build(),
                "Pay {1}{B} to return Tenacious Dead to the battlefield tapped?"));
    }
}
