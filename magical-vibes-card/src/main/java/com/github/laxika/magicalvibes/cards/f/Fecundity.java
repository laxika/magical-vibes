package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerMayDrawCardEffect;

@CardRegistration(set = "8ED", collectorNumber = "247")
public class Fecundity extends Card {

    public Fecundity() {
        // Whenever a creature dies, that creature's controller may draw a card.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DyingCreatureControllerMayDrawCardEffect());
    }
}
