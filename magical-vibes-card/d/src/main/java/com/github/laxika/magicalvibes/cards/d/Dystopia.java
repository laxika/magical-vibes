package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ALL", collectorNumber = "47")
@CardRegistration(set = "ME2", collectorNumber = "88")
public class Dystopia extends Card {

    public Dystopia() {
        // Cumulative upkeep—Pay 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.life(1));

        // At the beginning of each player's upkeep, that player sacrifices a green or white
        // permanent of their choice. Triggers.upkeep(ActivePlayer.ANY) sets the active player as the target
        // that sacrifices.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(
                1,
                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE)),
                SacrificeRecipient.ACTIVE_PLAYER));
    }
}
