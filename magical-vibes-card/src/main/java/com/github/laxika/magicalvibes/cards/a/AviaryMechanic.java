package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "KLD", collectorNumber = "6")
public class AviaryMechanic extends Card {

    public AviaryMechanic() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "permanent"
                ),
                "Return another permanent you control to its owner's hand?"
        ));
    }
}
