package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaSearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MRD", collectorNumber = "27")
public class TajNarSwordsmith extends Card {

    public TajNarSwordsmith() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayXManaSearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
    }
}
