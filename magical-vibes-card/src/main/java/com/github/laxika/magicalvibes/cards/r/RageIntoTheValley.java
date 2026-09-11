package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HOB", collectorNumber = "79")
public class RageIntoTheValley extends Card {

    public RageIntoTheValley() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new DrawCardEffect(1),
                new LoseLifeEffect(1),
                new AmassGoblinsEffect(2)));
    }
}
