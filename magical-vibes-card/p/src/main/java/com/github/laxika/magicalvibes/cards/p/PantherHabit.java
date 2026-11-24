package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddPlusCountersEffect;

@CardRegistration(set = "MSC", collectorNumber = "112")
@CardRegistration(set = "MSC", collectorNumber = "446")
public class PantherHabit extends Card {

    public PantherHabit() {
        addStaticEffect(PreventDamageAndAddPlusCountersEffect.preventingAndPutting(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
