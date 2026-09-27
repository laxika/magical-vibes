package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "18")
public class LandscaperColos extends Card {

    public LandscaperColos() {
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                        .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                        .targetGraveyard(true)
                        .build());

        addHandActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{W} ({1}{W}, Discard this card: Search your library for a basic land card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
