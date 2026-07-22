package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "233")
public class BloodtitheHarvester extends Card {

    public BloodtitheHarvester() {
        // When this creature enters, create a Blood token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));

        // {T}, Sacrifice this creature: Target creature gets -X/-X until end of turn, where X is
        // twice the number of Blood tokens you control. Activate only as a sorcery.
        Scaled minusTwiceBlood = new Scaled(
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsTokenPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD)
                        )),
                        CountScope.CONTROLLER),
                -2);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new BoostTargetCreatureEffect(minusTwiceBlood, minusTwiceBlood)
                ),
                "{T}, Sacrifice this creature: Target creature gets -X/-X until end of turn, "
                        + "where X is twice the number of Blood tokens you control. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
