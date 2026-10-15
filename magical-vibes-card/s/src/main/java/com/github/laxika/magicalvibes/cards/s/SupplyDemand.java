package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "157")
public class SupplyDemand extends Card {

    public SupplyDemand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Supply — Create X 1/1 green Saproling creature tokens",
                        new CreateTokenEffect(new XValue(), "Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                ).withManaCost("{X}{G}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Demand — Search your library for a multicolored card, reveal it, put it into your hand, then shuffle",
                        new SearchLibraryEffect(new CardIsMulticoloredPredicate())
                ).withManaCost("{1}{W}{U}")
        )));
    }
}
