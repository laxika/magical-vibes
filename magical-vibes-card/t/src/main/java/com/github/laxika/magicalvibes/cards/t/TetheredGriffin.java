package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "21")
public class TetheredGriffin extends Card {

    public TetheredGriffin() {
        // "When you control no enchantments, sacrifice this creature."
        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsEnchantmentPredicate()))),
                new SacrificeSelfEffect());
    }
}
