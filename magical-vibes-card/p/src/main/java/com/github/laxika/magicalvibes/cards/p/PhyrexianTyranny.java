package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLS", collectorNumber = "118")
@CardRegistration(set = "2X2", collectorNumber = "262")
public class PhyrexianTyranny extends Card {

    public PhyrexianTyranny() {
        // Whenever a player draws a card, that player loses 2 life unless they pay {2}.
        addTrigger(Triggers.youDrawCard(), loseTwoUnlessPaysTwo());
        addTrigger(Triggers.opponentDrawsCard(), loseTwoUnlessPaysTwo());
    }

    private static UnlessPaysEffect loseTwoUnlessPaysTwo() {
        return UnlessPaysEffect.unlessPays(Subjects.triggeringPlayer(), "{2}",
                new LoseLifeEffect(2, Subjects.triggeringPlayer()));
    }
}
