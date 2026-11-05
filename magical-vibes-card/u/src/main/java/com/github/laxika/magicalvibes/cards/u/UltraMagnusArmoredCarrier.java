package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class UltraMagnusArmoredCarrier extends Card {

    public UltraMagnusArmoredCarrier() {
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, GrantScope.SELF)));
        addTrigger(Triggers.attacks(), new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsAttackingPredicate()),
                new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(8),
                new TransformSelfEffect()));
    }
}
