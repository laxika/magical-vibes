package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "185")
public class ThirstingRoots extends Card {

    public ThirstingRoots() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a basic land card, reveal it, put it into your hand, then shuffle",
                        new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                new ChooseOneEffect.ChooseOneOption("Proliferate", new ProliferateEffect())
        )));
    }
}
