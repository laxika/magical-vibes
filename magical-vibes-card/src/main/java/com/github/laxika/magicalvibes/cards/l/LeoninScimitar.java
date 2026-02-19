package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureYouControlTargetFilter;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "331")
public class LeoninScimitar extends Card {

    public LeoninScimitar() {
        addEffect(EffectSlot.STATIC, new BoostEquippedCreatureEffect(1, 1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new EquipEffect()),
                true,
                false,
                "Equip {1}",
                new CreatureYouControlTargetFilter(),
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
