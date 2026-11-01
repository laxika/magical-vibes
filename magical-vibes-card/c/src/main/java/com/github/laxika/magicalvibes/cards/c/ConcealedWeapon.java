package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "117")
public class ConcealedWeapon extends Card {

    public ConcealedWeapon() {
        addMorph("{2}{R}");
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new AttachSourceEquipmentToTargetCreatureEffect());
        addStaticEffect(new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{1}{R}"));
    }
}
