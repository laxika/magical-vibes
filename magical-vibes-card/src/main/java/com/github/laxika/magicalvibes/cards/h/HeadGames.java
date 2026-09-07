package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "148")
@CardRegistration(set = "ONS", collectorNumber = "155")
public class HeadGames extends Card {

    public HeadGames() {
        addEffect(EffectSlot.SPELL, new HeadGamesEffect());
    }
}
