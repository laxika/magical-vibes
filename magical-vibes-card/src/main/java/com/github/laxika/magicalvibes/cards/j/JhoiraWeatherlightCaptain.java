package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "197")
public class JhoiraWeatherlightCaptain extends Card {

    public JhoiraWeatherlightCaptain() {
        // Whenever you cast a historic spell, draw a card.
        // (Artifacts, legendaries, and Sagas are historic.)
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsHistoricPredicate(),
                List.of(new DrawCardEffect())
        ));
    }
}
