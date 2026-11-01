package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "154")
@CardRegistration(set = "SLD", collectorNumber = "1390")
@CardRegistration(set = "SPG", collectorNumber = "8")
@CardRegistration(set = "DSC", collectorNumber = "167")
public class RampagingFerocidon extends Card {

    public RampagingFerocidon() {
        // Players can't gain life.
        addStaticEffect(new PlayersCantGainLifeEffect());
        // Whenever another creature enters, Rampaging Ferocidon deals 1 damage to that creature's controller.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
