package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLS", collectorNumber = "118")
@CardRegistration(set = "2X2", collectorNumber = "262")
public class PhyrexianTyranny extends Card {

    public PhyrexianTyranny() {
        addTrigger(Triggers.youDrawCard(), LoseLifeUnlessPaysEffect.forTriggeringPlayer(2, 2));
        addTrigger(Triggers.opponentDrawsCard(), LoseLifeUnlessPaysEffect.forTriggeringPlayer(2, 2));
    }
}
