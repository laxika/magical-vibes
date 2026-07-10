package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "9ED", collectorNumber = "313")
public class ThranGolem extends Card {

    public ThranGolem() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Enchanted(),
                new StaticBoostEffect(2, 2,
                        Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE),
                        GrantScope.SELF)));
    }
}
