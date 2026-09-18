package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "TMC", collectorNumber = "119")
public class RaphsBravado extends Card {

    public RaphsBravado() {
        // During your turn, attacking creatures get +1/+0.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new StaticBoostEffect(1, 0, GrantScope.ALL_CREATURES,
                        new PermanentIsAttackingPredicate())));
    }
}
