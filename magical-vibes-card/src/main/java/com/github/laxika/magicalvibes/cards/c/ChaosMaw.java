package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "HOU", collectorNumber = "87")
public class ChaosMaw extends Card {

    public ChaosMaw() {
        // When this creature enters, it deals 3 damage to each OTHER creature (exclude the source).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MassDamageEffect(3, false, false,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
