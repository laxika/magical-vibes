package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

public class ToralfHammer extends Card {

    public ToralfHammer() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}{R}",
                        List.of(
                                new UnattachSourceEquipmentCost(),
                                new DealDamageToAnyTargetEffect(3),
                                ReturnToHandEffect.grantingEquipment()
                        ),
                        "{1}{R}, {T}, Unattach Toralf's Hammer: It deals 3 damage to any target. "
                                + "Return Toralf's Hammer to its owner's hand."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                3, 0, GrantScope.EQUIPPED_CREATURE,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        addActivatedAbility(new EquipActivatedAbility("{1}{R}"));
    }
}
