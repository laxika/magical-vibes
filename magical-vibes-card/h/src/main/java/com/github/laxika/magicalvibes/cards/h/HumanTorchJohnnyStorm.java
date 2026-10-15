package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "136")
public class HumanTorchJohnnyStorm extends Card {

    public HumanTorchJohnnyStorm() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new ConditionalEffect(
                new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.HERO)),
                new DealDamageToAnyTargetEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{R}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "Power-up — {6}{R}: Put three +1/+1 counters on Human Torch. Activate each power-up "
                        + "ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
