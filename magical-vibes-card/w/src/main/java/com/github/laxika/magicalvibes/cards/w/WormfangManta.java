package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "58")
public class WormfangManta extends Card {

    public WormfangManta() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SkipNextEffect(SkipKind.TURN));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ControllerExtraTurnEffect(1));
    }
}
