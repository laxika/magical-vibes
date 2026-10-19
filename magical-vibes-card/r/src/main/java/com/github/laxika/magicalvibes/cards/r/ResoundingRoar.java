package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "ALA", collectorNumber = "144")
public class ResoundingRoar extends Card {

    public ResoundingRoar() {
        // Target creature gets +3/+3 until end of turn. Targeting auto-derived from the effect's TargetSpec.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));

        // Cycling {5}{R}{G}{W} ({5}{R}{G}{W}, Discard this card: Draw a card.)
        // "When you cycle this card, target creature gets +6/+6 until end of turn." The reflexive trigger is
        // an ON_SELF_CYCLED trigger: it goes on the stack above the cycling draw, and with no legal target it
        // is skipped while the cycling draw still happens.
        addEffect(EffectSlot.ON_SELF_CYCLED, new BoostTargetCreatureEffect(6, 6));
        addCycling("{5}{R}{G}{W}");
    }
}
