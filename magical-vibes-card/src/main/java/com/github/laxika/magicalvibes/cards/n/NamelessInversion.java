package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;

@CardRegistration(set = "LRW", collectorNumber = "128")
public class NamelessInversion extends Card {

    public NamelessInversion() {
        // Target creature gets +3/-3 and loses all creature types until end of turn.
        // Both effects share the single auto-derived creature target.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, -3));
        addEffect(EffectSlot.SPELL, new LoseAllCreatureTypesEffect());
    }
}
