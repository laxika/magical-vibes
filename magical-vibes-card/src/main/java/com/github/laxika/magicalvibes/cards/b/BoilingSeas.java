package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "178")
public class BoilingSeas extends Card {

    public BoilingSeas() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
