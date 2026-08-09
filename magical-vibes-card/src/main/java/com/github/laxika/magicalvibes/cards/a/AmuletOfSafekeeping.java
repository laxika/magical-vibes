package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "M19", collectorNumber = "226")
public class AmuletOfSafekeeping extends Card {

    public AmuletOfSafekeeping() {
        addEffect(EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new CounterUnlessPaysEffect(1));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES,
                new PermanentIsTokenPredicate()));
    }
}
