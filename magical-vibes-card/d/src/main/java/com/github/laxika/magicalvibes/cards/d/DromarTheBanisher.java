package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllPermanentsOfChosenColorToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "INV", collectorNumber = "244")
public class DromarTheBanisher extends Card {

    public DromarTheBanisher() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayPayManaEffect("{2}{U}",
                        new ReturnAllPermanentsOfChosenColorToHandEffect(new PermanentIsCreaturePredicate()),
                        "Pay {2}{U}?"));
    }
}
