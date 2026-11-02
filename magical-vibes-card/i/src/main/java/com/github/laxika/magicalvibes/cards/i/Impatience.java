package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfDidntCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "197")
@CardRegistration(set = "UDS", collectorNumber = "88")
public class Impatience extends Card {

    public Impatience() {
        // At the beginning of each player's end step, if that player didn't cast a spell
        // this turn, this enchantment deals 2 damage to that player.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new DealDamageIfDidntCastSpellThisTurnEffect(2));
    }
}
