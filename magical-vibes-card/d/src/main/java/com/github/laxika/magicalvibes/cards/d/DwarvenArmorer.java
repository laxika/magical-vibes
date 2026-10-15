package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "50")
public class DwarvenArmorer extends Card {

    public DwarvenArmorer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ChooseOneForTargetCreatureEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Put a +0/+1 counter on it",
                                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ZERO_PLUS_ONE)),
                                new ChooseOneEffect.ChooseOneOption("Put a +1/+0 counter on it",
                                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ZERO))))),
                "{R}, {T}, Discard a card: Put a +0/+1 counter or a +1/+0 counter on target creature."
        ));
    }
}
