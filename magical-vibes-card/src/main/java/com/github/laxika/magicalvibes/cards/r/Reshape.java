package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "31")
public class Reshape extends Card {

    public Reshape() {
        addEffect(EffectSlot.SPELL,
                new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.ARTIFACT),
                LibrarySearchDestination.BATTLEFIELD,
                new ManaValueBound(false, 0)));
    }
}
