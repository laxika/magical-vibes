package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "5DN", collectorNumber = "141")
public class OpalineBracers extends Card {

    public OpalineBracers() {
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new CountersOnSource(CounterType.CHARGE),
                new CountersOnSource(CounterType.CHARGE),
                GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
