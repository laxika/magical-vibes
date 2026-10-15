package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "LCI", collectorNumber = "109")
public class GreedyFreebooter extends Card {

    public GreedyFreebooter() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ScryEffect(1),
                CreateTokenEffect.ofTreasureToken(1)));
    }
}
