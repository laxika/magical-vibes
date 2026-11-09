package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPCA", collectorNumber = "79")
public class TemberCity extends Card {

    public TemberCity() {
        // Whenever a player taps a land for mana, Tember City deals 1 damage to that player.
        addTrigger(Triggers.playerTapsLandForMana(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));
        addTrigger(Triggers.chaosEnsues(), new SacrificePermanentsEffect(
                1, new PermanentNotPredicate(new PermanentIsLandPredicate()), SacrificeRecipient.EACH_OPPONENT));
    }
}
