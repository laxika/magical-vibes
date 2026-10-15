package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

/**
 * Skull Rend — {3}{B}{R} Sorcery
 *
 * Skull Rend deals 2 damage to each opponent. Those players each discard two cards at random.
 */
@CardRegistration(set = "RTR", collectorNumber = "195")
public class SkullRend extends Card {

    public SkullRend() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.EACH_OPPONENT, true));
    }
}
