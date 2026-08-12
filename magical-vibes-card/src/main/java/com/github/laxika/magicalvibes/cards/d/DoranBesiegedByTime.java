package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "215")
@CardRegistration(set = "ECL", collectorNumber = "334")
public class DoranBesiegedByTime extends Card {

    public DoranBesiegedByTime() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardToughnessGreaterThanPowerPredicate())),
                1,
                CostModificationScope.SELF));

        DynamicAmount difference = new Max(
                new Sum(new TargetPower(), new Scaled(new TargetToughness(), -1)),
                new Sum(new TargetToughness(), new Scaled(new TargetPower(), -1)));
        BoostTargetCreatureEffect boost = new BoostTargetCreatureEffect(difference, difference);
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, boost);
        addEffect(EffectSlot.ON_ANY_CREATURE_BLOCKS, new TriggeringPermanentConditionalEffect(
                new PermanentControlledBySourceControllerPredicate(), boost));
    }
}
