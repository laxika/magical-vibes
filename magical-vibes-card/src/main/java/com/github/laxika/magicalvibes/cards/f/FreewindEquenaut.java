package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "9")
public class FreewindEquenaut extends Card {

    public FreewindEquenaut() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Enchanted(),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new DealDamageToTargetCreatureEffect(2)),
                                "{T}: This creature deals 2 damage to target attacking or blocking creature.",
                                new PermanentPredicateTargetFilter(
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsAttackingPredicate(),
                                                new PermanentIsBlockingPredicate()
                                        )),
                                        "Target must be an attacking or blocking creature"
                                )
                        ),
                        GrantScope.SELF
                )
        ));
    }
}
