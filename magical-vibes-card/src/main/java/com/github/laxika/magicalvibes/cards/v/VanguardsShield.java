package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "AVR", collectorNumber = "223")
public class VanguardsShield extends Card {

    public VanguardsShield() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 3, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
