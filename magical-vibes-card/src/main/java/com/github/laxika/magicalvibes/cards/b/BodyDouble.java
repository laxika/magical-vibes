package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "PLC", collectorNumber = "35")
public class BodyDouble extends Card {

    public BodyDouble() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CopyPermanentOnEnterEffect.fromAnyGraveyard(
                        new CardTypePredicate(CardType.CREATURE), "creature card"));
    }
}
