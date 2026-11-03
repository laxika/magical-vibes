package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "215")
@CardRegistration(set = "ECL", collectorNumber = "334")
public class DoranBesiegedByTime extends Card {

    public DoranBesiegedByTime() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardToughnessGreaterThanPowerPredicate())),
                1,
                CostModificationScope.SELF));

        DynamicAmount difference = new Max(
                new Sum(new TargetPower(true), new Scaled(new TargetToughness(), -1)),
                new Sum(new TargetToughness(), new Scaled(new TargetPower(true), -1)));
        BoostTargetCreatureEffect boost = new BoostTargetCreatureEffect(difference, difference);
        addTrigger(Triggers.creatureYouControlAttacks(), boost);
        addTrigger(Triggers.creatureBlocks().withFilter(new PermanentControlledBySourceControllerPredicate()), boost);
    }
}
