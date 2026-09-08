package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "129")
public class RazorBoomerang extends Card {

    public RazorBoomerang() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(
                                new UnattachSourceEquipmentCost(),
                                new DealDamageToAnyTargetEffect(1),
                                ReturnToHandEffect.grantingEquipment()
                        ),
                        "{T}, Unattach Razor Boomerang: It deals 1 damage to any target. "
                                + "Return Razor Boomerang to its owner's hand."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
