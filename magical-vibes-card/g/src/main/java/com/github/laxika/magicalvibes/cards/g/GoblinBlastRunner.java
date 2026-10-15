package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "137")
public class GoblinBlastRunner extends Card {

    public GoblinBlastRunner() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerSacrificedPermanentThisTurn(),
                new StaticBoostEffect(2, 0, Set.of(Keyword.MENACE), GrantScope.SELF)));
    }
}
