package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "150")
public class AfterburnerExpert extends Card {

    public AfterburnerExpert() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Exhaust — {2}{G}{G}: Put two +1/+1 counters on this creature."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY,
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false));
    }
}
