package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "271")
public class CompanionOfTheTrials extends Card {

    public CompanionOfTheTrials() {
        // {1}{W}: Untap target creature. Activate only if you control a Gideon planeswalker.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "{1}{W}: Untap target creature. Activate only if you control a Gideon planeswalker.",
                TargetFilters.creature()
        ).withRequiredControlledPermanents(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.GIDEON))),
                1,
                "Gideon planeswalkers"));
    }
}
