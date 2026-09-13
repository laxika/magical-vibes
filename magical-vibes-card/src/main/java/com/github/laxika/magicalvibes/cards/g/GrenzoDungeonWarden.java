package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomCardAndReturnIfCreaturePowerAtMostSourceEffect;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "205")
public class GrenzoDungeonWarden extends Card {

    public GrenzoDungeonWarden() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new MillBottomCardAndReturnIfCreaturePowerAtMostSourceEffect()),
                "{2}: Put the bottom card of your library into your graveyard. If it's a creature card "
                        + "with power less than or equal to Grenzo's power, put it onto the battlefield."
        ));
    }
}
