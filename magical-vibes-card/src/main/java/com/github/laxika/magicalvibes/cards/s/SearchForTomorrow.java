package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "216")
public class SearchForTomorrow extends Card {

    public SearchForTomorrow() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(),
                "Suspend 2\u2014{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));
    }
}
