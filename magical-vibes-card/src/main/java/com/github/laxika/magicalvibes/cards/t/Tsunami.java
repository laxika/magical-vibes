package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "334")
@CardRegistration(set = "4ED", collectorNumber = "278")
public class Tsunami extends Card {

    public Tsunami() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
