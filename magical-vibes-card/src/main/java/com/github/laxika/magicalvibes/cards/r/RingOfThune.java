package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "213")
public class RingOfThune extends Card {

    public RingOfThune() {
        // Equipped creature has vigilance.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));

        // At the beginning of your upkeep, put a +1/+1 counter on equipped creature if it's white.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnReferencedPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentColorInPredicate(Set.of(CardColor.WHITE))));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
