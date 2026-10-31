package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "MH1", collectorNumber = "67")
public class ScourAllPossibilities extends Card {

    public ScourAllPossibilities() {
        addSpellEffect(new ScryEffect(2));
        addSpellEffect(new DrawCardEffect(1));
        addCastingOption(new FlashbackCast("{4}{U}"));
    }
}
