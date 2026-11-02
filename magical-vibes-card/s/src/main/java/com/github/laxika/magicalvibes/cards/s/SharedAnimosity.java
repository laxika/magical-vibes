package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.OtherAttackersSharingCreatureTypeWithTarget;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "104")
@CardRegistration(set = "E02", collectorNumber = "29")
@CardRegistration(set = "WOT", collectorNumber = "49")
@CardRegistration(set = "LTC", collectorNumber = "226")
@CardRegistration(set = "LCC", collectorNumber = "231")
@CardRegistration(set = "C20", collectorNumber = "158")
public class SharedAnimosity extends Card {

    public SharedAnimosity() {
        // Whenever a creature you control attacks, it gets +1/+0 until end of turn for each other
        // attacking creature that shares a creature type with it. The attacking creature is the
        // trigger's (non-targeting) target; the boost scales with the count computed at resolution.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new BoostTargetCreatureEffect(new OtherAttackersSharingCreatureTypeWithTarget(), new Fixed(0)));
    }
}
