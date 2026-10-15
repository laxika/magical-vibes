package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "143")
@CardRegistration(set = "PIP", collectorNumber = "671")
@CardRegistration(set = "PIP", collectorNumber = "1057")
public class StrengthBobblehead extends Card {

    public StrengthBobblehead() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {3}, {T}: Put X +1/+1 counters on target creature, where X is the number of Bobbleheads
        // you control. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.BOBBLEHEAD),
                                CountScope.CONTROLLER))),
                "{3}, {T}: Put X +1/+1 counters on target creature, where X is the number of "
                        + "Bobbleheads you control. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
