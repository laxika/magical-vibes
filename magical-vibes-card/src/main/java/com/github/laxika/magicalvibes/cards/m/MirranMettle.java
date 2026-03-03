package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftReplacementEffect;

@CardRegistration(set = "MBS", collectorNumber = "84")
public class MirranMettle extends Card {

    public MirranMettle() {
        // Target creature gets +2/+2 until end of turn.
        // Metalcraft — That creature gets +4/+4 until end of turn instead if you control three or more artifacts.
        addEffect(EffectSlot.SPELL, new MetalcraftReplacementEffect(
                new BoostTargetCreatureEffect(2, 2),
                new BoostTargetCreatureEffect(4, 4)
        ));
    }
}
