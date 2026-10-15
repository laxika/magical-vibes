package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EquipmentDamagesOtherDefendingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "260")
public class KusariGama extends Card {

    public KusariGama() {
        // Equipped creature has "{2}: This creature gets +1/+0 until end of turn."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(new BoostSelfEffect(1, 0)),
                        "{2}: This creature gets +1/+0 until end of turn."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Whenever equipped creature deals damage to a blocking creature, this Equipment deals that
        // much damage to each other creature defending player controls.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new EquipmentDamagesOtherDefendingCreaturesEffect());

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
