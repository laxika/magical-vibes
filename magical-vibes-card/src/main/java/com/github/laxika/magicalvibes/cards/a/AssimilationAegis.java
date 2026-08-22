package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "192")
public class AssimilationAegis extends Card {

    public AssimilationAegis() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());
        addEffect(EffectSlot.ON_EQUIPMENT_ATTACHED_TO_CREATURE,
                new AttachedCreatureBecomesCopyOfExiledCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
