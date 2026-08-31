package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "55")
public class NadirKraken extends Card {

    public NadirKraken() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new MayPayManaEffect("{1}",
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new CreateTokenEffect("Tentacle", 1, 1, CardColor.BLUE,
                                        List.of(CardSubtype.TENTACLE), Set.of(), Set.of())),
                        "Pay {1} to put a +1/+1 counter on Nadir Kraken and create a 1/1 blue Tentacle creature token?"));
    }
}
