package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "DIS", collectorNumber = "160")
public class BronzeBombshell extends Card {

    public BronzeBombshell() {
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> {
                    UUID ownerId = sourcePermanent.getCard().getOwnerId();
                    return ownerId != null && !ownerId.equals(controllerId);
                },
                List.of(new SacrificeSelfThenEffect(
                        new DealDamageToPlayersEffect(7, DamageRecipient.CONTROLLER))),
                "Bronze Bombshell's state-triggered ability"
        ));
    }
}
