package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "27")
public class TempestraDameOfGames extends Card {

    public TempestraDameOfGames() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "Sacrifice an artifact"),
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(), Set.of(), null, null, Map.of(),
                                true, false, true, false,
                                false, false, null, Set.of(),
                                false, Map.of(), List.of(), false, true,
                                new Fixed(1), false, Set.of(), false)
                ),
                "{2}{R}, {T}, Sacrifice an artifact: Create a token that's a copy of another target creature "
                        + "you control, except it isn't legendary. It gains haste. Sacrifice it at the beginning "
                        + "of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature you control")
        ));
    }
}
