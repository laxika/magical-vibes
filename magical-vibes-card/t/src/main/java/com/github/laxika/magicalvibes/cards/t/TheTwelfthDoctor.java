package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "164")
@CardRegistration(set = "WHO", collectorNumber = "449")
@CardRegistration(set = "WHO", collectorNumber = "563")
@CardRegistration(set = "WHO", collectorNumber = "769")
@CardRegistration(set = "WHO", collectorNumber = "1040")
@CardRegistration(set = "WHO", collectorNumber = "1154")
public class TheTwelfthDoctor extends Card {

    public TheTwelfthDoctor() {
        addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND).nthThisTurn(1).grantedToTheSpell(),
                new MayEffect(new DemonstrateEffect(), "Copy this spell for an opponent?"));
        addTrigger(Triggers.youCopySpell().anySpell(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
