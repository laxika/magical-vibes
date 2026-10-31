package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "193")
public class PrishesWanderings extends Card {

    public PrishesWanderings() {
        addSpellEffect(new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        CardPredicateUtils.basicLand(),
                        new CardSubtypePredicate(CardSubtype.TOWN)
                )),
                LibrarySearchDestination.BATTLEFIELD_TAPPED
        ));
        // When you search your library this way, put a +1/+1 counter on target creature you control.
        addSpellEffect(new QueueReflexiveAbilityEffect(
                PutCounterOnTargetPermanentEffect.withTargetRestriction(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate())))));
    }
}
