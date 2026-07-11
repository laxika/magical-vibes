package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "183")
@CardRegistration(set = "POR", collectorNumber = "129")
public class Flashfires extends Card {

    public Flashfires() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentHasSubtypePredicate(CardSubtype.PLAINS)));
    }
}
