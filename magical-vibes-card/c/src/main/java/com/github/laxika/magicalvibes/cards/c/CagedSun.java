package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "132")
@CardRegistration(set = "BRR", collectorNumber = "9")
@CardRegistration(set = "C14", collectorNumber = "233")
@CardRegistration(set = "40K", collectorNumber = "231")
@CardRegistration(set = "SLD", collectorNumber = "2461")
@CardRegistration(set = "CM2", collectorNumber = "178")
public class CagedSun extends Card {

    public CagedSun() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addStaticEffect(new BoostCreaturesOfChosenColorEffect(1, 1));
        addTrigger(Triggers.youTapLandForMana(), new AddExtraManaOfChosenColorOnLandTapEffect());
    }
}
