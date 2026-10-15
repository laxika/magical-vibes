package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawCreateBearReplacementEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "305")
public class WordsOfWilding extends Card {

    public WordsOfWilding() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RegisterNextDrawCreateBearReplacementEffect()),
                "{1}: The next time you would draw a card this turn, create a 2/2 green Bear creature token instead."));
    }
}
