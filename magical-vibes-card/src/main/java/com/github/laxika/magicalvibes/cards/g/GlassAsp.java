package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseLifeAtNextDrawStepUnlessPaysEffect;

@CardRegistration(set = "TSP", collectorNumber = "197")
public class GlassAsp extends Card {

    public GlassAsp() {
        // Whenever this creature deals combat damage to a player, that player loses 2 life at the
        // beginning of their next draw step unless they pay {2} before that step.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new RegisterLoseLifeAtNextDrawStepUnlessPaysEffect(2, 2));
    }
}
