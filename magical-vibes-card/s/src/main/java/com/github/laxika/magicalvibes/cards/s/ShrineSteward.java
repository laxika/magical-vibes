package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "259")
public class ShrineSteward extends Card {

    public ShrineSteward() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.AURA),
                                new CardSubtypePredicate(CardSubtype.SHRINE)
                        ))),
                        "Search your library for an Aura or Shrine card?"));
    }
}
