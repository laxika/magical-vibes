package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M12", collectorNumber = "110")
@CardRegistration(set = "M20", collectorNumber = "325")
public class SorinsThirst extends Card {

    public SorinsThirst() {
        // Sorin's Thirst deals 2 damage to target creature and you gain 2 life.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
