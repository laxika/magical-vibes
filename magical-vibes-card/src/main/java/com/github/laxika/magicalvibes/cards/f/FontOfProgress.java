package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "51")
public class FontOfProgress extends Card {

    public FontOfProgress() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MillEffect(new CountersOnSource(CounterType.OIL), MillRecipient.TARGET_PLAYER)),
                "{3}, {T}: Target player mills X cards, where X is the number of oil counters on this artifact."
        ));
    }
}
