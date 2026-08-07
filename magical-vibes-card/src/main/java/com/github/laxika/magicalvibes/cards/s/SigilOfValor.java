package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ORI", collectorNumber = "239")
public class SigilOfValor extends Card {

    public SigilOfValor() {
        // Whenever equipped creature attacks alone, it gets +1/+1 until end of turn for each other
        // creature you control. The amounts are evaluated from the equipped creature, so
        // excludeSource models "each other creature".
        PermanentCount otherCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true);
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new AttacksAlone(),
                new BoostEquippedCreatureUntilEndOfTurnEffect(otherCreatures, otherCreatures)));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
