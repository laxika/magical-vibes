package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ControlledLandsEnterUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveLandExtraManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "19")
public class VigorousFarming extends Card {

    public VigorousFarming() {
        addStaticEffect(new ControlledLandsEnterUntappedEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PerpetuallyGiveLandExtraManaEffect(ManaColor.GREEN));
    }
}
