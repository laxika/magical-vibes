package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddPlusCountersEffect;

@CardRegistration(set = "MSC", collectorNumber = "112")
@CardRegistration(set = "MSC", collectorNumber = "446")
public class PantherHabit extends Card {

    public PantherHabit() {
        addEffect(EffectSlot.STATIC, new PreventDamageAndAddPlusCountersEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
