package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOU", collectorNumber = "70")
public class MaraudingBoneslasher extends Card {

    public MaraudingBoneslasher() {
        // This creature can't block unless you control another Zombie.
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new ControlsOtherPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                "you control another Zombie"
        ));
    }
}
