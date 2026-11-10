package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCardPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "67")
@CardRegistration(set = "LCC", collectorNumber = "99")
public class ArmMountedAnchor extends Card {

    public ArmMountedAnchor() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(2),
                new DiscardTwoUnlessCardPredicateEffect(new CardSubtypePredicate(CardSubtype.PIRATE)));
        addActivatedAbility(new EquipActivatedAbility("{2}", new ReduceActivationCostEffect(
                new FixedIfCondition(new CardsInHandAtMost(1), 2, 0))));
    }
}
