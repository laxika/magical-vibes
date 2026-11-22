package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FUT", collectorNumber = "119")
public class ShahOfNaarIsle extends Card {

    public ShahOfNaarIsle() {
        addStaticEffect(new EchoEffect("{0}",
                        List.of(EachPlayerMayDrawUpToNGainLifePerCardBelowEffect.drawUpToInTurnOrder(
                3, Subjects.eachOpponent()))));
    }
}
