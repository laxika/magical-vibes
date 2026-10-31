package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "41")
public class InactionInjunction extends Card {

    public InactionInjunction() {
        // Detain target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN))
                // Draw a card.
                .addSpellEffect(new DrawCardEffect(1));
    }
}
