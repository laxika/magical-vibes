package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEC", collectorNumber = "10")
@CardRegistration(set = "NEC", collectorNumber = "45")
public class SwiftReconfiguration extends Card {

    public SwiftReconfiguration() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                "Target must be a creature or Vehicle"))
                .addEffect(EffectSlot.STATIC,
                        new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.VEHICLE, GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                null,
                                List.of(new CrewCost(5), AnimatePermanentsEffect.crew()),
                                "Crew 5"),
                        GrantScope.ENCHANTED_PERMANENT));
    }
}
