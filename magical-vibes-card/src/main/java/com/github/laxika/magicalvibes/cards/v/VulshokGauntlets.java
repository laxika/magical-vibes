package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MRD", collectorNumber = "273")
public class VulshokGauntlets extends Card {

    public VulshokGauntlets() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
