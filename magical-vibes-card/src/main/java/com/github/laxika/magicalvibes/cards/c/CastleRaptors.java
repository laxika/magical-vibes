package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "TSP", collectorNumber = "5")
public class CastleRaptors extends Card {

    public CastleRaptors() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new StaticBoostEffect(0, 2, GrantScope.SELF)));
    }
}
