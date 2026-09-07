package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FIN", collectorNumber = "6")
@CardRegistration(set = "FIN", collectorNumber = "325")
@CardRegistration(set = "FIN", collectorNumber = "424")
public class AmbrosiaWhiteheart extends Card {

    public AmbrosiaWhiteheart() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "permanent"
                ),
                "Return another permanent you control to its owner's hand?"
        ));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0));
    }
}
