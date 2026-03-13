package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "28")
public class SerraAscendant extends Card {

    public SerraAscendant() {
        // As long as you have 30 or more life, Serra Ascendant gets +5/+5 and has flying.
        addEffect(EffectSlot.STATIC, new ControllerLifeThresholdConditionalEffect(
                30, new StaticBoostEffect(5, 5, Set.of(Keyword.FLYING), GrantScope.SELF)));
    }
}
