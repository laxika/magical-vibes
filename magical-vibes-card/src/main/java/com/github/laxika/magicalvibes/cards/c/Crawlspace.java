package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatAttackTargetScope;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "ULG", collectorNumber = "123")
public class Crawlspace extends Card {

    public Crawlspace() {
        addEffect(EffectSlot.STATIC,
                new MaximumCombatCreaturesEffect(2, Integer.MAX_VALUE, CombatAttackTargetScope.CONTROLLER));
    }
}
