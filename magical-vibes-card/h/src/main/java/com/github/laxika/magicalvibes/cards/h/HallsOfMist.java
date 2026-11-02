package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedDuringControllersLastTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "354")
public class HallsOfMist extends Card {

    public HallsOfMist() {
        // Cumulative upkeep {1}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        // Creatures that attacked during their controller's last turn can't attack — global, so the
        // exemption is "did not attack during its controller's previous turn".
        addStaticEffect(new CreaturesCantAttackUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentAttackedDuringControllersLastTurnPredicate())));
    }
}
