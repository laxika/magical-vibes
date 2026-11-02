package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "126")
@CardRegistration(set = "2X2", collectorNumber = "115")
@CardRegistration(set = "PIO", collectorNumber = "143")
public class LabyrinthChampion extends Card {

    public LabyrinthChampion() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new DealDamageToAnyTargetEffect(2)),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
