package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2XM", collectorNumber = "75")
@CardRegistration(set = "C14", collectorNumber = "20")
public class WellOfIdeas extends Card {

    public WellOfIdeas() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(2));
        addTrigger(Triggers.drawStep(ActivePlayer.OPPONENT), new DrawCardForTargetPlayerEffect(1));
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DrawCardEffect(2));
    }
}
