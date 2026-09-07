package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawGainLifeReplacementEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "61")
public class WordsOfWorship extends Card {

    public WordsOfWorship() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RegisterNextDrawGainLifeReplacementEffect()),
                "{1}: The next time you would draw a card this turn, you gain 5 life instead."));
    }
}
