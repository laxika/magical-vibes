package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "24")
public class LightstallInquisitor extends Card {

    public LightstallInquisitor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                EachOpponentExilesFromHandEffect.withPlayPermission(1, 1, true));
    }
}
