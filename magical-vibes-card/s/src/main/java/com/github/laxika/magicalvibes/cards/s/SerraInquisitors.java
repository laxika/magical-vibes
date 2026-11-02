package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "16")
public class SerraInquisitors extends Card {

    public SerraInquisitors() {
        // Whenever this creature blocks or becomes blocked by one or more black creatures,
        // this creature gets +2/+0 until end of turn.
        PermanentColorInPredicate black = new PermanentColorInPredicate(Set.of(CardColor.BLACK));
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK,
                new BoostSelfWhenCombatOpponentMatchesEffect(black, 2, 0));
        addTrigger(Triggers.becomesBlocked(), new BoostSelfWhenCombatOpponentMatchesEffect(black, 2, 0));
    }
}
