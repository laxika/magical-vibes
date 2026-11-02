package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "42")
@CardRegistration(set = "SLD", collectorNumber = "260")
@CardRegistration(set = "VOC", collectorNumber = "107")
public class KamiOfTheCrescentMoon extends Card {

    public KamiOfTheCrescentMoon() {
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardForTargetPlayerEffect(1));
    }
}
