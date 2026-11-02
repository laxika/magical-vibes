package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardIfOddMayCastFreeElseDrawEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "363")
@CardRegistration(set = "CMM", collectorNumber = "596")
@CardRegistration(set = "C18", collectorNumber = "51")
public class YennettCrypticSovereign extends Card {

    public YennettCrypticSovereign() {
        addTrigger(Triggers.attacks(), new RevealTopCardIfOddMayCastFreeElseDrawEffect());
    }
}
