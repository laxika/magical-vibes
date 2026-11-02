package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "9ED", collectorNumber = "124")
@CardRegistration(set = "8ED", collectorNumber = "126")
@CardRegistration(set = "MMQ", collectorNumber = "130")
public class Deathgazer extends Card {

    public Deathgazer() {
        // Whenever this creature blocks or becomes blocked by a nonblack creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonblack = new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        addTrigger(Triggers.blocks(), new DestroyCombatOpponentAtEndOfCombatEffect(nonblack, false));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(nonblack, false));
    }
}
