package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "21")
public class TetheredGriffin extends Card {

    public TetheredGriffin() {
        // "When you control no enchantments, sacrifice this creature."
        addTrigger(Triggers.state(), StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                List.of(new SacrificeSelfEffect()),
                "Tethered Griffin's state-triggered ability"
        ));
    }
}
