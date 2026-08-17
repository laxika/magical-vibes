package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "GRN", collectorNumber = "90")
public class WhisperingSnitch extends Card {

    public WhisperingSnitch() {
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS, new OncePerTurnTriggerEffect(
                SequenceEffect.of(
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1))));
    }
}
