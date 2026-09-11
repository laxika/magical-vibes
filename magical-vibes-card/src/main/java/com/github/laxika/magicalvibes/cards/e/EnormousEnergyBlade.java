package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "NEO", collectorNumber = "96")
public class EnormousEnergyBlade extends Card {

    public EnormousEnergyBlade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPMENT_ATTACHED,
                new TapPermanentsEffect(TapUntapScope.ENCHANTED));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
