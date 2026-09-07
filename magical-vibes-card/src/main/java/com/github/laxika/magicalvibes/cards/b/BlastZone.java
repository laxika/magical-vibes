package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "258")
@CardRegistration(set = "WAR", collectorNumber = "244")
public class BlastZone extends Card {

    public BlastZone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(1)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}",
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE, new XValue())),
                "{X}{X}, {T}: Put X charge counters on Blast Zone."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect()
                ),
                "{3}, {T}, Sacrifice Blast Zone: Destroy each nonland permanent with mana value equal to the number of charge counters on Blast Zone."
        ));
    }
}
