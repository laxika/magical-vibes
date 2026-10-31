package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostByAttackCountEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "YMKM", collectorNumber = "18")
public class RampagingUrsaguana extends Card {

    public RampagingUrsaguana() {
        addMorph("{2}{G}{G}");
        // Ward {2}. While face down (disguised) the engine grants ward {2} to the permanent.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addStaticEffect(BoostByAttackCountEffect.forEachAttackThisGame(2, 2, GrantScope.SELF));
    }
}
