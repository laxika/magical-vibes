package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "534")
public class BoomerangBladeFlinger extends Card {

    public BoomerangBladeFlinger() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(1)));
    }
}
