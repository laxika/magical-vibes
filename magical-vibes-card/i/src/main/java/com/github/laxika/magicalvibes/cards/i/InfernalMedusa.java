package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "108")
public class InfernalMedusa extends Card {

    public InfernalMedusa() {
        addTrigger(Triggers.blocks(), new DestroyCombatOpponentAtEndOfCombatEffect(false));

        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addTrigger(Triggers.becomesBlocked().withFilter(nonWall),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(false));
    }
}
