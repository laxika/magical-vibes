package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "8ED", collectorNumber = "237")
public class Choke extends Card {

    public Choke() {
        // Islands don't untap during their controllers' untap steps.
        addEffect(EffectSlot.STATIC,
                new MatchingPermanentsDoesntUntapEffect(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
