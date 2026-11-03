package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "279")
public class RockBasilisk extends Card {

    public RockBasilisk() {
        // Whenever this creature blocks or becomes blocked by a non-Wall creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addTrigger(Triggers.blocks(), new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false));
    }
}
