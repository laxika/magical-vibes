package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXChargeCountersEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "141")
public class ChimericMass extends Card {

    public ChimericMass() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXChargeCountersEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AnimateSelfByChargeCountersEffect(List.of(CardSubtype.CONSTRUCT))),
                "{1}: Until end of turn, Chimeric Mass becomes a Construct artifact creature with \"This creature's power and toughness are each equal to the number of charge counters on it.\""
        ));
    }
}
