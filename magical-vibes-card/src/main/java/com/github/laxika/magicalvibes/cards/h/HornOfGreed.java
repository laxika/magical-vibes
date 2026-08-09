package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;

@CardRegistration(set = "STH", collectorNumber = "135")
public class HornOfGreed extends Card {

    public HornOfGreed() {
        // Whenever a player plays a land, that player draws a card.
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new DrawCardEffect());
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND, new DrawCardForTargetPlayerEffect(1));
    }
}
