package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ICE", collectorNumber = "281")
public class AltarOfBone extends Card {

    public AltarOfBone() {
        // As an additional cost to cast this spell, sacrifice a creature.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        // Search your library for a creature card, reveal it, put it into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.HAND));
    }
}
