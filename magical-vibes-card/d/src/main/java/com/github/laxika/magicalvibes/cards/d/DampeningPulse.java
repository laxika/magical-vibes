package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "BFZ", collectorNumber = "75")
public class DampeningPulse extends Card {

    public DampeningPulse() {
        addStaticEffect(new StaticBoostEffect(-1, 0, GrantScope.OPPONENT_CREATURES));
    }
}
