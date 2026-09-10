package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "156")
public class MinionOfTheMighty extends Card {

    public MinionOfTheMighty() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new AttackingCreaturesTotalPowerAtLeast(6),
                PutCardToBattlefieldEffect.tappedAndAttacking(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.DRAGON))), "Dragon creature")));
    }
}
