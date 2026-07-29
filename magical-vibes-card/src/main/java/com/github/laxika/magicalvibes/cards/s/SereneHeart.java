package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MIR", collectorNumber = "242")
public class SereneHeart extends Card {

    public SereneHeart() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentHasSubtypePredicate(CardSubtype.AURA)));
    }
}
