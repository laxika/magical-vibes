package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "164")
@CardRegistration(set = "HML", collectorNumber = "48")
@CardRegistration(set = "ME2", collectorNumber = "91")
public class FuneralMarch extends Card {

    public FuneralMarch() {
        // Enchant creature
        target(TargetFilters.creature())
        // When enchanted creature leaves the battlefield, its controller sacrifices a creature of their choice.
        .addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
