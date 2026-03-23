package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "XLN", collectorNumber = "282")
public class GraspingCurrent extends Card {

    public GraspingCurrent() {
        // Return up to two target creatures to their owner's hand.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ), 0, 2).addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandEffect());

        // Search your library and/or graveyard for a card named Jace, Ingenious Mind-Mage,
        // reveal it, and put it into your hand. If you searched your library this way, shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Jace, Ingenious Mind-Mage"));
    }
}
