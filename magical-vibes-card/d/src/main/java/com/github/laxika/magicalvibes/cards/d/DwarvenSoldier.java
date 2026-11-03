package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "221")
@CardRegistration(set = "FEM", collectorNumber = "53")
@CardRegistration(set = "FEM", collectorNumber = "107")
@CardRegistration(set = "FEM", collectorNumber = "108")
@CardRegistration(set = "FEM", collectorNumber = "109")
@CardRegistration(set = "ME1", collectorNumber = "92")
public class DwarvenSoldier extends Card {

    public DwarvenSoldier() {
        // Whenever this creature blocks or becomes blocked by one or more Orcs,
        // this creature gets +0/+2 until end of turn.
        PermanentHasSubtypePredicate orc = new PermanentHasSubtypePredicate(CardSubtype.ORC);
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new BoostSelfWhenCombatOpponentMatchesEffect(orc, 0, 2));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfWhenCombatOpponentMatchesEffect(orc, 0, 2));
    }
}
