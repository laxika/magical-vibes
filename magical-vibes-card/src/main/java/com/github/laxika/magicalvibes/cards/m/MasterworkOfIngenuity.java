package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "863")
public class MasterworkOfIngenuity extends Card {

    public MasterworkOfIngenuity() {
        // You may have this Equipment enter as a copy of any Equipment on the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), "Equipment"
        ));
    }
}
