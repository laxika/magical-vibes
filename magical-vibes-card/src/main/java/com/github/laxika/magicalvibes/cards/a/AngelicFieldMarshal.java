package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "2")
public class AngelicFieldMarshal extends Card {

    public AngelicFieldMarshal() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControlledCommanderAsCast(),
                new StaticBoostEffect(2, 2, Set.of(Keyword.VIGILANCE), GrantScope.ALL_OWN_CREATURES)));
    }
}
