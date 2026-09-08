package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "IKO", collectorNumber = "99")
public class SerratedScorpion extends Card {

    public SerratedScorpion() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                new GainLifeEffect(2)));
    }
}
