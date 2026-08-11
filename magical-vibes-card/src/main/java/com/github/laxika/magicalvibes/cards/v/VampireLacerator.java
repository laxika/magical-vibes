package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "ZEN", collectorNumber = "115")
public class VampireLacerator extends Card {

    public VampireLacerator() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ConditionalEffect.unless(
                new NotCondition(new AnOpponentLifeAtMost(10)), new LoseLifeEffect(1)));
    }
}
