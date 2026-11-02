package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "226")
public class AmuletOfSafekeeping extends Card {

    public AmuletOfSafekeeping() {
        addTrigger(Triggers.youBecomeTargetOfOpponentSpellOrAbility(),
                new CounterUnlessPaysEffect(1));
        addStaticEffect(new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES,
                new PermanentIsTokenPredicate()));
    }
}
