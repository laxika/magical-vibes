package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "210")
public class BitterWork extends Card {

    public BitterWork() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(4)
                        ))),
                        new DrawCardEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new EarthbendTargetLandEffect(4)),
                "Exhaust — {4}: Earthbend 4. Activate only during your turn. (Activate each exhaust ability only once.)",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
