package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "45")
@CardRegistration(set = "SLD", collectorNumber = "478")
@CardRegistration(set = "WOT", collectorNumber = "25")
@CardRegistration(set = "WOT", collectorNumber = "71")
@CardRegistration(set = "WOT", collectorNumber = "91")
@CardRegistration(set = "FCA", collectorNumber = "31")
public class RhysticStudy extends Card {

    public RhysticStudy() {
        addTrigger(Triggers.opponentCastsSpell(), new DrawCardUnlessPaysEffect(1, 1));
    }
}
