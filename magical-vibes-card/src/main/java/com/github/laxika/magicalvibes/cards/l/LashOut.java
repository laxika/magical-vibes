package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "LRW", collectorNumber = "183")
public class LashOut extends Card {

    public LashOut() {
        // Deal 3 damage to the target creature, then clash. On a win, deal 3 damage to that
        // creature's controller. The creature stays on the battlefield through resolution (lethal
        // damage is destroyed only after all effects finish), so TARGET_PERMANENT_CONTROLLER still
        // finds its controller even when the first 3 damage was lethal.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.SPELL, new ClashEffect(null));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new WonClash(),
                new DealDamageToPlayersEffect(3, DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
