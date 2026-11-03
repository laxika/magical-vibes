package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.UUID;

@CardRegistration(set = "DIS", collectorNumber = "160")
public class BronzeBombshell extends Card {

    public BronzeBombshell() {
        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> {
                    UUID ownerId = sourcePermanent.getCard().getOwnerId();
                    return ownerId != null && !ownerId.equals(controllerId);
                }),
                new SacrificeSelfThenEffect(
                        new DealDamageToPlayersEffect(7, DamageRecipient.CONTROLLER)));
    }
}
