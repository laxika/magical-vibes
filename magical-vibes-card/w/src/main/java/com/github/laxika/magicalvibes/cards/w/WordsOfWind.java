package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawReturnPermanentsReplacementEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "122")
public class WordsOfWind extends Card {

    public WordsOfWind() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RegisterNextDrawReturnPermanentsReplacementEffect()),
                "{1}: The next time you would draw a card this turn, each player returns a permanent they control to its owner's hand instead."));
    }
}
