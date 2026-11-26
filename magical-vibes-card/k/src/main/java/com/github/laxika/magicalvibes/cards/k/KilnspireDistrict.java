package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "23")
@CardRegistration(set = "OPCA", collectorNumber = "44")
public class KilnspireDistrict extends Card {

    public KilnspireDistrict() {
        SequenceEffect addChargeAndMana = SequenceEffect.of(
                PutCountersEffect.onSelf(CounterType.CHARGE),
                new AwardManaEffect(ManaColor.RED, new CountersOnSource(CounterType.CHARGE)));
        addTrigger(Triggers.planeswalkToThis(), addChargeAndMana);
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), addChargeAndMana);
        addTrigger(Triggers.chaosEnsues(), UnlessPaysEffect.youMayPayX("{X}", DealDamageEffect.toAnyTarget(new XValue())));
    }
}
