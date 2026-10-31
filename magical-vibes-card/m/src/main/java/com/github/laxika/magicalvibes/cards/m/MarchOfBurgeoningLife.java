package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForTargetCreatureNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueLessThanXPredicate;

@CardRegistration(set = "NEO", collectorNumber = "201")
public class MarchOfBurgeoningLife extends Card {

    public MarchOfBurgeoningLife() {
        addCastingCost(new ExileAnyNumberOfCardsFromHandCost(
                new CardColorPredicate(CardColor.GREEN), 2));
        addSpellEffect(new SearchLibraryForTargetCreatureNameToBattlefieldEffect(
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                new PermanentManaValueLessThanXPredicate(),
                true));
    }
}
