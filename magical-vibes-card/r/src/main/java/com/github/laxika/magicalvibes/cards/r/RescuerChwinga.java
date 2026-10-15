package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "HBG", collectorNumber = "101")
public class RescuerChwinga extends Card {

    public RescuerChwinga() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "permanent"
                ),
                "Return another permanent you control to its owner's hand?"
        ));
    }
}
