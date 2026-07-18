package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.UntappedLandsAtTurnStart;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "4ED", collectorNumber = "216")
public class PowerSurge extends Card {

    public PowerSurge() {
        // At the beginning of each player's upkeep, deal X damage to that player, where X is the
        // number of untapped lands they controlled at the beginning of this turn. EACH_UPKEEP_TRIGGERED
        // sets the active player as target; UntappedLandsAtTurnStart reads the turn-start snapshot so
        // tapping lands in response can't reduce the damage (CR ruling).
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(new UntappedLandsAtTurnStart(), DamageRecipient.TARGET_PLAYER));
    }
}
