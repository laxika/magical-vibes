package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "GRN", collectorNumber = "77")
public class MidnightReaper extends Card {

    public MidnightReaper() {
        SequenceEffect deathEffect = SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER),
                new DrawCardEffect(1));

        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, deathEffect);
        addEffect(EffectSlot.ON_DEATH, deathEffect);
    }
}
