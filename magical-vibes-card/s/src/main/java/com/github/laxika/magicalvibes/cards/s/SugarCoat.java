package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "76")
public class SugarCoat extends Card {

    public SugarCoat() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD)
                )),
                "Target must be a creature or Food"
        ))
                .addStaticEffect(new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.ENCHANTED_PERMANENT))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.FOOD, GrantScope.ENCHANTED_PERMANENT, true))
                .addStaticEffect(new BecomeColorlessEffect(GrantScope.ENCHANTED_PERMANENT))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_PERMANENT))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this artifact: You gain 3 life."
                        ),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
