package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ULG", collectorNumber = "45")
public class Tinker extends Card {

    public Tinker() {
        // As an additional cost to cast this spell, sacrifice an artifact.
        addEffect(EffectSlot.SPELL, new SacrificePermanentCost(
                new PermanentIsArtifactPredicate(), "Sacrifice an artifact"));

        // Search your library for an artifact card, put that card onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.ARTIFACT), LibrarySearchDestination.BATTLEFIELD));
    }
}
