package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;

@CardRegistration(set = "ELD", collectorNumber = "141")
@CardRegistration(set = "SLD", collectorNumber = "813")
@CardRegistration(set = "SLD", collectorNumber = "814")
@CardRegistration(set = "SLD", collectorNumber = "815")
@CardRegistration(set = "SLD", collectorNumber = "816")
@CardRegistration(set = "SLD", collectorNumber = "817")
@CardRegistration(set = "SLD", collectorNumber = "818")
@CardRegistration(set = "SLD", collectorNumber = "819")
public class SevenDwarves extends Card {

    public SevenDwarves() {
        addEffect(EffectSlot.STATIC, new BoostByOtherCreaturesWithSameNameEffect(1, 1, true));
    }
}
