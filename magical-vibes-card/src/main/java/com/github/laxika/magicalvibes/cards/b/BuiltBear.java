package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "335")
@CardRegistration(set = "MB2", collectorNumber = "572")
public class BuiltBear extends Card {

    public BuiltBear() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1,
                3,
                Set.of(Keyword.FLASH, Keyword.DEATHTOUCH, Keyword.REACH, Keyword.VIGILANCE, Keyword.WARD),
                GrantScope.SELF));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
    }
}
