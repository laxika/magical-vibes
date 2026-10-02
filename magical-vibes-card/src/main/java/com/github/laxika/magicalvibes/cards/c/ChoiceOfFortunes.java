package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.SeekTwoCardsThenMayShuffleAndSeekEffect;

@CardRegistration(set = "YSNC", collectorNumber = "5")
public class ChoiceOfFortunes extends Card {

    public ChoiceOfFortunes() {
        addEffect(EffectSlot.SPELL, new SeekTwoCardsThenMayShuffleAndSeekEffect());
        addEffect(EffectSlot.SPELL, new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME));
    }
}
