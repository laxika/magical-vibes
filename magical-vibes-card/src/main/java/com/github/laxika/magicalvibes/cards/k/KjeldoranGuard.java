package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSacrificeSourceWhenTargetLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "35")
public class KjeldoranGuard extends Card {

    public KjeldoranGuard() {
        // {T}: Target creature gets +1/+1 until end of turn. When that creature leaves the
        // battlefield this turn, sacrifice this creature. Activate only during combat and only
        // if defending player controls no snow lands.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new BoostTargetCreatureEffect(1, 1),
                        new RegisterDelayedSacrificeSourceWhenTargetLeavesEffect()
                ),
                "{T}: Target creature gets +1/+1 until end of turn. When that creature leaves the "
                        + "battlefield this turn, sacrifice this creature. Activate only during combat "
                        + "and only if defending player controls no snow lands.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_COMBAT
        ).withActivationCondition(
                new NotCondition(new DefendingPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW)
                )))),
                "Activate only if defending player controls no snow lands"));
    }
}
