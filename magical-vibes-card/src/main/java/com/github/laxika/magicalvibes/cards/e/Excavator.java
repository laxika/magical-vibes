package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.GrantLandwalkOfSacrificedLandToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "287")
public class Excavator extends Card {

    public Excavator() {
        // {T}, Sacrifice a basic land: Target creature gains landwalk of each of the land types of
        // the sacrificed land until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.BASIC))),
                                "a basic land"),
                        new GrantLandwalkOfSacrificedLandToTargetEffect()
                ),
                "{T}, Sacrifice a basic land: Target creature gains landwalk of each of the land "
                        + "types of the sacrificed land until end of turn.",
                TargetFilters.creature()
        ));
    }
}
