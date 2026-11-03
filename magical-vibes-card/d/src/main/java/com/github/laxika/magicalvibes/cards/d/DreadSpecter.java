package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "119")
public class DreadSpecter extends Card {

    public DreadSpecter() {
        // Whenever this creature blocks or becomes blocked by a nonblack creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonblack = new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        addTrigger(Triggers.blocks(), new DestroyCombatOpponentAtEndOfCombatEffect(nonblack, false));
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(nonblack, false));
    }
}
