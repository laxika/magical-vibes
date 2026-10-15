package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "225")
public class ViashinoSandswimmer extends Card {

    public ViashinoSandswimmer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new FlipCoinWinEffect(ReturnToHandEffect.self(), new SacrificeSelfEffect())),
                "{R}: Flip a coin. If you win the flip, return this creature to its owner's hand. "
                        + "If you lose the flip, sacrifice this creature."
        ));
    }
}
