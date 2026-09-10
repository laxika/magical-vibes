package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "DSK", collectorNumber = "19")
public class LionheartGlimmer extends Card {

    public LionheartGlimmer() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
