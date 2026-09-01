package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "172")
public class TheEarthKing extends Card {

    public TheEarthKing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Bear", 4, 4, CardColor.GREEN,
                        List.of(CardSubtype.BEAR), Set.of(), Set.of()));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumMatchingAttackers(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4)
                ))),
                new SearchLibraryEffect(new EventValue(), CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED)));
    }
}
