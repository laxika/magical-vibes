package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "132")
public class SilverskinArmor extends Card {

    public SilverskinArmor() {
        // Equipped creature gets +1/+1
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        // Equipped creature is an artifact in addition to its other types
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
