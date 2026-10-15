package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MOR", collectorNumber = "146")
public class VeteransArmaments extends Card {

    public VeteransArmaments() {
        // Equipped creature has "Whenever this creature attacks or blocks, it gets +1/+1 until end
        // of turn for each attacking creature." Counts every attacking creature (any controller).
        PermanentCount attackingCreatures = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.ON_ATTACK,
                new BoostEquippedCreatureUntilEndOfTurnEffect(attackingCreatures, attackingCreatures));
        addEffect(EffectSlot.ON_BLOCK,
                new BoostEquippedCreatureUntilEndOfTurnEffect(attackingCreatures, attackingCreatures));

        // Whenever a Soldier creature enters, you may attach this Equipment to it.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SOLDIER),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
