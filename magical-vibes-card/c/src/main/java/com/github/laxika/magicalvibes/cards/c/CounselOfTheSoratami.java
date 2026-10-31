package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "76")
@CardRegistration(set = "CHK", collectorNumber = "55")
@CardRegistration(set = "9ED", collectorNumber = "69")
public class CounselOfTheSoratami extends Card {

    public CounselOfTheSoratami() {
        addSpellEffect(new DrawCardEffect(2));
    }
}
