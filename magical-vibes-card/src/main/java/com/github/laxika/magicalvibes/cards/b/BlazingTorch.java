package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "216")
public class BlazingTorch extends Card {

    public BlazingTorch() {
        // Equipped creature can't be blocked by Vampires or Zombies
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentNotPredicate(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)
                        ))
                ),
                "non-Vampire, non-Zombie creatures"
        ));

        // Equipped creature has "{T}, Sacrifice Blazing Torch: Blazing Torch deals 2 damage to any target."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSourceEquipmentCost(), new DealDamageToAnyTargetEffect(2)),
                        "{T}, Sacrifice Blazing Torch: Blazing Torch deals 2 damage to any target."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
