package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.AnimateChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "181")
@CardRegistration(set = "LCI", collectorNumber = "379")
public class CosmiumConfluence extends Card {

    public CosmiumConfluence() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a Cave card, put it onto the battlefield tapped, then shuffle.",
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.CAVE),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put three +1/+1 counters on a Cave you control. It becomes a 0/0 Elemental creature with haste. It's still a land.",
                        SequenceEffect.of(
                                new PutCounterOnChosenOwnPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3,
                                        new PermanentHasSubtypePredicate(CardSubtype.CAVE)),
                                new AnimateChosenPermanentEffect(0, 0, List.of(CardSubtype.ELEMENTAL),
                                        Set.of(Keyword.HASTE)))),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target enchantment.",
                        new DestroyTargetPermanentEffect(), TargetFilters.enchantment())
        ), 3));
    }
}
