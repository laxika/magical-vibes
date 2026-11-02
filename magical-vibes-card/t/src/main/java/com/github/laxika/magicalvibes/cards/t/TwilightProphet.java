package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndDrainOpponentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "88")
@CardRegistration(set = "SLD", collectorNumber = "1304")
@CardRegistration(set = "CMM", collectorNumber = "193")
@CardRegistration(set = "CMM", collectorNumber = "524")
@CardRegistration(set = "MKC", collectorNumber = "143")
@CardRegistration(set = "LCC", collectorNumber = "211")
public class TwilightProphet extends Card {

    public TwilightProphet() {
        addStaticEffect(new AscendEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(new ControllerHasCityBlessing(),
                new RevealTopCardPutIntoHandAndDrainOpponentsEffect()));
    }
}
