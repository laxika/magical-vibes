package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "153")
public class BlindingPowder extends Card {

    public BlindingPowder() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        null,
                        List.of(new UnattachSourceEquipmentCost(), PreventDamageEffect.allCombatToSelf()),
                        "Unattach Blinding Powder: Prevent all combat damage that would be dealt to this creature this turn."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
