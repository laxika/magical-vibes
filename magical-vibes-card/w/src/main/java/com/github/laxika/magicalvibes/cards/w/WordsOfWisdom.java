package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;

@CardRegistration(set = "ODY", collectorNumber = "114")
public class WordsOfWisdom extends Card {

    public WordsOfWisdom() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new EachOtherPlayerDrawsCardEffect(1));
    }
}
