package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "161")
@CardRegistration(set = "ANB", collectorNumber = "82")
@CardRegistration(set = "UMA", collectorNumber = "142")
@CardRegistration(set = "WOT", collectorNumber = "47")
public class RaidBombardment extends Card {

    public RaidBombardment() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentPowerAtMostPredicate(2),
                        new DealDamageToAttackedTargetEffect(1)));
    }
}
