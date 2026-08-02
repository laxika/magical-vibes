package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "GTC", collectorNumber = "7")
public class BorosElite extends Card {

    public BorosElite() {
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new MinimumAttackers(3), new BoostSelfEffect(2, 2)));
    }
}
