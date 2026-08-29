package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "66")
public class DeathmaskNezumi extends Card {

    public DeathmaskNezumi() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CardsInHandAtLeast(7),
                new StaticBoostEffect(2, 1, Set.of(Keyword.FEAR), GrantScope.SELF)));
    }
}
