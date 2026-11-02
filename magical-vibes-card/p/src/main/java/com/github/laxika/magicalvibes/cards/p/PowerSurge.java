package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.UntappedLandsAtTurnStart;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "216")
@CardRegistration(set = "SUM", collectorNumber = "170")
@CardRegistration(set = "3ED", collectorNumber = "170")
@CardRegistration(set = "2ED", collectorNumber = "168")
@CardRegistration(set = "LEA", collectorNumber = "167")
@CardRegistration(set = "LEB", collectorNumber = "168")
public class PowerSurge extends Card {

    public PowerSurge() {
        // At the beginning of each player's upkeep, deal X damage to that player, where X is the
        // number of untapped lands they controlled at the beginning of this turn. Triggers.upkeep(ActivePlayer.ANY)
        // sets the active player as target; UntappedLandsAtTurnStart reads the turn-start snapshot so
        // tapping lands in response can't reduce the damage.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(new UntappedLandsAtTurnStart(), DamageRecipient.ACTIVE_PLAYER));
    }
}
