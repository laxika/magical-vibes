package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "21")
@CardRegistration(set = "CMD", collectorNumber = "12")
public class CrescendoOfWar extends Card {

    public CrescendoOfWar() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new PutCountersOnSelfEffect(CounterType.STRIFE));

        addStaticEffect(new StaticBoostEffect(
                1, 0, GrantScope.ALL_CREATURES_INCLUDING_SELF, new PermanentIsAttackingPredicate(), CounterType.STRIFE, false));
        addStaticEffect(new StaticBoostEffect(
                1, 0, GrantScope.ALL_OWN_CREATURES, new PermanentIsBlockingPredicate(), CounterType.STRIFE, false));
    }
}
