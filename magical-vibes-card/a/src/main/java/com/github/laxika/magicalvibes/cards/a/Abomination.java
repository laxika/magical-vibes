package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "117")
@CardRegistration(set = "LEG", collectorNumber = "87")
public class Abomination extends Card {

    public Abomination() {
        // Whenever this creature blocks or becomes blocked by a green or white creature,
        // destroy that creature at end of combat.
        PermanentColorInPredicate greenOrWhite =
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE));
        addTrigger(Triggers.blocks().withFilter(greenOrWhite), new DestroyCombatOpponentAtEndOfCombatEffect(false));
        addTrigger(Triggers.becomesBlocked().withFilter(greenOrWhite),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(false));
    }
}
