package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "ARB", collectorNumber = "34")
public class BituminousBlast extends Card {

    public BituminousBlast() {
        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());

        // Bituminous Blast deals 4 damage to target creature.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
    }
}
