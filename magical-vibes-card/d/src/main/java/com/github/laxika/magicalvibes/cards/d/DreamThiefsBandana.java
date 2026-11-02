package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "38")
@CardRegistration(set = "OTC", collectorNumber = "74")
public class DreamThiefsBandana extends Card {

    public DreamThiefsBandana() {
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect.forSourceController());
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
