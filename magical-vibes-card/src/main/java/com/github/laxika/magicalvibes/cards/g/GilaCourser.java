package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "OTJ", collectorNumber = "124")
public class GilaCourser extends Card {

    public GilaCourser() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceIsSaddled(), new ExileTopCardsMayPlayUntilNextTurnEffect(1)));
    }
}
