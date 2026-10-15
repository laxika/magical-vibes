package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "61")
public class SeaGodsRevenge extends Card {

    public SeaGodsRevenge() {
        // Return up to three target creatures your opponents control to their owners' hands.
        target(TargetFilters.creatureAnOpponentControls(), 0, 3)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Scry 1.
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
