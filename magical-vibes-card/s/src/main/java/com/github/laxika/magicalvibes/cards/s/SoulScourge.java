package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "85")
public class SoulScourge extends Card {

    public SoulScourge() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RememberTargetPlayerEffect(),
                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new TargetPlayerGainsLifeEffect(3));
    }
}
