package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "123")
public class JourneyOfDiscovery extends Card {

    public JourneyOfDiscovery() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}{G}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for up to two basic land cards, reveal them, and put them into your hand",
                        new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption(
                        "You may play up to two additional lands this turn",
                        new PlayAdditionalLandsEffect(2))
        )));
    }
}
