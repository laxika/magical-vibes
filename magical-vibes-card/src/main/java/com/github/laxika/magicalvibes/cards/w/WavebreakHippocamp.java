package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "80")
public class WavebreakHippocamp extends Card {

    public WavebreakHippocamp() {
        // Whenever you cast your first spell during each opponent's turn, draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect(1)),
                        null, null, null, true, false, null, 1));
    }
}
