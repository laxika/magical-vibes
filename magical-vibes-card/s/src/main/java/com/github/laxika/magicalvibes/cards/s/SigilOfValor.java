package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "239")
@CardRegistration(set = "BRR", collectorNumber = "53")
public class SigilOfValor extends Card {

    public SigilOfValor() {
        // Whenever equipped creature attacks alone, it gets +1/+1 until end of turn for each other
        // creature you control. The amounts are evaluated from the equipped creature, so
        // excludeSource models "each other creature".
        PermanentCount otherCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true);
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new BoostEquippedCreatureUntilEndOfTurnEffect(otherCreatures, otherCreatures));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
