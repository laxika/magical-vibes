package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "213")
public class SorcerersStrongbox extends Card {

    public SorcerersStrongbox() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new FlipCoinWinEffect(new SacrificeSelfAndDrawCardsEffect(3))),
                "{2}, {T}: Flip a coin. If you win the flip, sacrifice Sorcerer's Strongbox and draw three cards."
        ));
    }
}
