package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C15", collectorNumber = "40")
public class VerdantConfluence extends Card {

    public VerdantConfluence() {
        setAllowSharedTargets(true);

        var permanentCardInGraveyard = new GraveyardCardPredicateTargetFilter(
                new CardIsPermanentPredicate(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModes(List.of(
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Put two +1/+1 counters on target creature",
                        () -> new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        TargetFilters.creature()),
                ChooseOneEffect.ChooseOneOption.withEffectFactory(
                        "Return target permanent card from your graveyard to your hand",
                        () -> ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsPermanentPredicate())
                                .targetGraveyard(true)
                                .build(),
                        permanentCardInGraveyard),
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a basic land card, put it onto the battlefield tapped, then shuffle",
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED))
        ), 3));
    }
}
