package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "108")
public class InfernalMedusa extends Card {

    public InfernalMedusa() {
        PermanentIsCreaturePredicate anyCreature = new PermanentIsCreaturePredicate();
        addTrigger(Triggers.blocks(), new DestroyCombatOpponentAtEndOfCombatEffect(anyCreature, false));

        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false));
    }
}
