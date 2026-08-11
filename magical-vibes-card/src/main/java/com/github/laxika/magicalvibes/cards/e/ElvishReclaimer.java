package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "169")
public class ElvishReclaimer extends Card {

    public ElvishReclaimer() {
        // This creature gets +2/+2 as long as there are three or more land cards in your graveyard.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(3, new CardTypePredicate(CardType.LAND)),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));

        // {2}, {T}, Sacrifice a land: Search your library for a land card, put it onto the
        // battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "a land"),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.LAND),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{2}, {T}, Sacrifice a land: Search your library for a land card, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
