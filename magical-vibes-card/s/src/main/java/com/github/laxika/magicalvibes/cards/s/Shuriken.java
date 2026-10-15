package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerGainsControlOfGrantingEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "160")
public class Shuriken extends Card {

    public Shuriken() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(
                                new UnattachSourceEquipmentCost(),
                                new DealDamageToTargetCreatureEffect(2),
                                new TargetPermanentControllerGainsControlOfGrantingEquipmentEffect(
                                        ControlDuration.PERMANENT, CardSubtype.NINJA)
                        ),
                        "{T}, Unattach Shuriken: Shuriken deals 2 damage to target creature. "
                                + "That creature's controller gains control of Shuriken unless it was unattached from a Ninja.",
                        TargetFilters.creature()
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
