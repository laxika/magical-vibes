package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MRD", collectorNumber = "273")
@CardRegistration(set = "2XM", collectorNumber = "305")
public class VulshokGauntlets extends Card {

    public VulshokGauntlets() {
        addStaticEffect(new StaticBoostEffect(4, 2, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(DoesntUntapEffect.enchanted());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
