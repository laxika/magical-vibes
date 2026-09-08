package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KHM", collectorNumber = "237")
public class CosmosElixir extends Card {

    public CosmosElixir() {
        ControllerLifeAtLeast condition = new ControllerLifeAtLeast(GameData.STARTING_LIFE_TOTAL + 1);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                ConditionalEffect.unless(condition, new DrawCardEffect()),
                ConditionalEffect.unless(new NotCondition(condition), new GainLifeEffect(2))));
    }
}
