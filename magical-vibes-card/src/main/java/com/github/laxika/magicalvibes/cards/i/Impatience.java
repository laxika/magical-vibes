package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfDidntCastSpellThisTurnEffect;

@CardRegistration(set = "7ED", collectorNumber = "197")
public class Impatience extends Card {

    public Impatience() {
        // At the beginning of each player's end step, if that player didn't cast a spell
        // this turn, this enchantment deals 2 damage to that player.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new DealDamageIfDidntCastSpellThisTurnEffect(2));
    }
}
