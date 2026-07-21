package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ARB", collectorNumber = "98")
public class GloryOfWarfare extends Card {

    public GloryOfWarfare() {
        // During your turn, creatures you control get +2/+0.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(2, 0, GrantScope.OWN_CREATURES)));

        // During turns other than yours, creatures you control get +0/+2.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new NotControllerTurn(),
                new StaticBoostEffect(0, 2, GrantScope.OWN_CREATURES)));
    }
}
