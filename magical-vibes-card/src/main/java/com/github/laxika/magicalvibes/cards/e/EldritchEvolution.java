package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "INR", collectorNumber = "195")
public class EldritchEvolution extends Card {

    public EldritchEvolution() {
        // As an additional cost to cast this spell, sacrifice a creature.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(true));
        // Search your library for a creature card with mana value X or less, where X is 2 plus the
        // sacrificed creature's mana value. Put that card onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.CREATURE),
                LibrarySearchDestination.BATTLEFIELD,
                new ManaValueBound(false, 2)));
        // Exile Eldritch Evolution.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
