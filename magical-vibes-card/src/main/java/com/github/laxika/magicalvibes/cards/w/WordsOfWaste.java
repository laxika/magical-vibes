package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawDiscardOpponentsReplacementEffect;
import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "182")
public class WordsOfWaste extends Card {

    public WordsOfWaste() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RegisterNextDrawDiscardOpponentsReplacementEffect()),
                "{1}: The next time you would draw a card this turn, each opponent discards a card instead."));
    }
}
