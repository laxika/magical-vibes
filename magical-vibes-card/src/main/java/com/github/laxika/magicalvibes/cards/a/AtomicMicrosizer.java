package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "47")
public class AtomicMicrosizer extends Card {

    public AtomicMicrosizer() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new MakeCreatureUnblockableEffect())
                .addEffect(EffectSlot.ON_ATTACK, new SetBasePowerToughnessEffect(1, 1));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
