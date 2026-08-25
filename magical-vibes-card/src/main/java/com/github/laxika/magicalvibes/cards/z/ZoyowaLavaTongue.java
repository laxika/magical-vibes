package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrSacrificePermanentEffect;

@CardRegistration(set = "LCI", collectorNumber = "245")
public class ZoyowaLavaTongue extends Card {

    public ZoyowaLavaTongue() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new DescendedThisTurn(),
                        new EachOpponentMayDiscardOrSacrificePermanentEffect(3)));
    }
}
