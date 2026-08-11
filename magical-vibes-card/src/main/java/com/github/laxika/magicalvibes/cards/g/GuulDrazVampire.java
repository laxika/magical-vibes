package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "93")
public class GuulDrazVampire extends Card {

    public GuulDrazVampire() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnOpponentLifeAtMost(10),
                new StaticBoostEffect(2, 1, Set.of(Keyword.INTIMIDATE), GrantScope.SELF)));
    }
}
