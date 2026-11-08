package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "146")
public class VeteransArmaments extends Card {

    public VeteransArmaments() {
        // Equipped creature has "Whenever this creature attacks or blocks, it gets +1/+1 until end
        // of turn for each attacking creature." Counts every attacking creature (any controller).
        // The granted ability belongs to the equipped creature, so its controller controls the trigger.
        setAttackTriggerGrantedByEquipment(true);
        PermanentCount attackingCreatures = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER);
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()),
                new BoostEquippedCreatureUntilEndOfTurnEffect(attackingCreatures, attackingCreatures));

        // Whenever a Soldier creature enters, you may attach this Equipment to it.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER))),
                        ObjectRelation.ANOTHER),
                new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                        "Attach this Equipment to it?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
