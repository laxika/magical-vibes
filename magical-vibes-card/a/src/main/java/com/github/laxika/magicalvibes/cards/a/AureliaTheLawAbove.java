package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "188")
@CardRegistration(set = "MKM", collectorNumber = "317")
@CardRegistration(set = "MKM", collectorNumber = "357")
public class AureliaTheLawAbove extends Card {

    public AureliaTheLawAbove() {
        addTrigger(Triggers.playerAttacks(),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new MinimumAttackers(3)),
                new DrawCardEffect());
        addTrigger(Triggers.playerAttacks(),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new MinimumAttackers(5)),
                SequenceEffect.of(
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(3)));
    }
}
