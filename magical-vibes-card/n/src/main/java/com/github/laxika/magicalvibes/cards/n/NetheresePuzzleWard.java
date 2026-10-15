package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD4Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "AFC", collectorNumber = "17")
public class NetheresePuzzleWard extends Card {

    public NetheresePuzzleWard() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RollD4Effect(null, new ScryEffect(new EventValue())));
        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_HIGHEST_NATURAL_RESULT,
                new DrawCardEffect());
    }
}
