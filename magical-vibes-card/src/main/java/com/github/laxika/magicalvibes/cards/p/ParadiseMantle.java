package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "5DN", collectorNumber = "142")
public class ParadiseMantle extends Card {

    public ParadiseMantle() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                GrantScope.EQUIPPED_CREATURE
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
