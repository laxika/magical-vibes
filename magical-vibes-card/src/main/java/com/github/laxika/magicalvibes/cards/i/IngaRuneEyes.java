package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "KHM", collectorNumber = "64")
public class IngaRuneEyes extends Card {

    public IngaRuneEyes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(3));
        addEffect(EffectSlot.ON_DEATH, ConditionalEffect.unless(
                new CreaturesDiedThisTurnAtLeast(3), new DrawCardEffect(3)));
    }
}
