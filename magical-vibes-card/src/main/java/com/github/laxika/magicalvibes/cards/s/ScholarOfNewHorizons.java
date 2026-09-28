package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "252")
public class ScholarOfNewHorizons extends Card {

    public ScholarOfNewHorizons() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(),
                        new SearchLibraryEffect(
                                new CardSubtypePredicate(CardSubtype.PLAINS),
                                LibrarySearchDestination.HAND,
                                true)
                ),
                "{T}, Remove a counter from a permanent you control: Search your library for a Plains card and reveal it. "
                        + "If an opponent controls more lands than you, you may put that card onto the battlefield tapped. "
                        + "If you don't put the card onto the battlefield, put it into your hand. Then shuffle."
        ));
    }
}
