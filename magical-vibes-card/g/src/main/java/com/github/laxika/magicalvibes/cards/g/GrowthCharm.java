package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "77")
public class GrowthCharm extends Card {

    public GrowthCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Rampant Growth",
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardSupertypePredicate(CardSupertype.BASIC),
                                        new CardTypePredicate(CardType.LAND))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                new ChooseOneEffect.ChooseOneOption(
                        "Giant Growth",
                        new BoostTargetCreatureEffect(3, 3),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Regrowth",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        )));
    }
}
