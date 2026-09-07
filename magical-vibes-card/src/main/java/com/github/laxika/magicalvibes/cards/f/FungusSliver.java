package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "195")
public class FungusSliver extends Card {

    public FungusSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        GrantTriggeredAbilityEffect damagedSliverGetsCounter = new GrantTriggeredAbilityEffect(
                EffectSlot.ON_DEALT_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                GrantScope.ALL_CREATURES,
                sliver);

        addEffect(EffectSlot.STATIC, damagedSliverGetsCounter);
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_DEALT_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                GrantScope.SELF,
                sliver));
    }
}
