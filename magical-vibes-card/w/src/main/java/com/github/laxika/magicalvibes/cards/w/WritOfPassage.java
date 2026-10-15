package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreaturePowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "37")
public class WritOfPassage extends Card {

    public WritOfPassage() {
        PermanentAllOfPredicate powerAtMostTwoCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(2)
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsHostOfSourceAuraPredicate(),
                                powerAtMostTwoCreature
                        )),
                        new ConditionalEffect(
                                new NotCondition(new EnchantedCreaturePowerAtLeast(3)),
                                new MakeCreatureUnblockableEffect()
                        )));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new MakeCreatureUnblockableEffect()),
                "Forecast — {1}{U}, Reveal this card from your hand: Target creature with power 2 or less can't be blocked this turn. "
                        + "Activate only during your upkeep and only once each turn.",
                new PermanentPredicateTargetFilter(powerAtMostTwoCreature,
                        "Target must be a creature with power 2 or less"),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
