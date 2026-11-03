package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "136")
@CardRegistration(set = "DMR", collectorNumber = "86")
public class FleshReaver extends Card {

    public FleshReaver() {
        DealDamageToPlayersEffect damageBack =
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CONTROLLER);
        addTrigger(List.of(
                        Triggers.creatureYouControlDealsDamageToCreature(),
                        Triggers.sourceYouControlDealsDamageToOpponent()),
                damageBack);
    }
}
