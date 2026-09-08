package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "211")
public class NaturesEmbrace extends Card {

    public NaturesEmbrace() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()
                )),
                "Target must be a creature or land"))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE),
                        null
                ))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentIsLandPredicate(),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new AwardAnyColorManaEffect(2)),
                                        "{T}: Add two mana of any one color."
                                ),
                                GrantScope.ENCHANTED_PERMANENT
                        ),
                        null
                ));
    }
}
