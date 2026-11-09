package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "256")
@CardRegistration(set = "LEB", collectorNumber = "219")
@CardRegistration(set = "5ED", collectorNumber = "331")
@CardRegistration(set = "4ED", collectorNumber = "274")
@CardRegistration(set = "2ED", collectorNumber = "219")
@CardRegistration(set = "SUM", collectorNumber = "218")
@CardRegistration(set = "ME1", collectorNumber = "134")
@CardRegistration(set = "3ED", collectorNumber = "218")
@CardRegistration(set = "LEA", collectorNumber = "218")
public class ThicketBasilisk extends Card {

    public ThicketBasilisk() {
        // Whenever this creature blocks or becomes blocked by a non-Wall creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addTrigger(Triggers.blocks().withFilter(nonWall), new DestroyCombatOpponentAtEndOfCombatEffect(false));
        addTrigger(Triggers.becomesBlocked().withFilter(nonWall),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(false));
    }
}
