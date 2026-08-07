package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnGrantingEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "253")
public class Hankyu extends Card {

    public Hankyu() {
        // Equipped creature has "{T}: Put an aim counter on Hankyu." The counters go on the
        // Equipment, not on the creature activating the ability.
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new PutCountersOnGrantingEquipmentEffect(CounterType.AIM)),
                        "{T}: Put an aim counter on Hankyu."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equipped creature has "{T}, Remove all aim counters from Hankyu: This creature deals
        // damage to any target equal to the number of aim counters removed this way." The removal
        // is an additional cost that snapshots the count into X; the damage source is the creature.
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new RemoveAllCountersAsCostEffect(CounterType.AIM, true),
                                new DealDamageToAnyTargetEffect(new XValue())),
                        "{T}, Remove all aim counters from Hankyu: This creature deals damage to any target "
                                + "equal to the number of aim counters removed this way."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
