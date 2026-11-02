package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "66")
@CardRegistration(set = "SOC", collectorNumber = "212")
@CardRegistration(set = "C15", collectorNumber = "121")
@CardRegistration(set = "DSC", collectorNumber = "138")
public class DoomwakeGiant extends Card {

    public DoomwakeGiant() {
        // Constellation — Whenever this creature or another enchantment you control enters,
        // creatures your opponents control get -1/-1 until end of turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new BoostAllCreaturesEffect(-1, -1,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
