package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "111")
@CardRegistration(set = "TSR", collectorNumber = "117")
@CardRegistration(set = "C19", collectorNumber = "116")
public class GorgonRecluse extends Card {

    public GorgonRecluse() {
        // Whenever this creature blocks or becomes blocked by a nonblack creature,
        // destroy that creature at end of combat.
        PermanentNotPredicate nonblack = new PermanentNotPredicate(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        addTrigger(Triggers.blocks().withFilter(nonblack), new DestroyCombatOpponentAtEndOfCombatEffect(false));
        addTrigger(Triggers.becomesBlocked().withFilter(nonblack),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(false));

        // Madness {B}{B}
        addCastingOption(new MadnessCast("{B}{B}"));
    }
}
