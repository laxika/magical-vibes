package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

/**
 * Frenetic Efreet — {1}{U}{R} Creature — Efreet 2/1.
 * "Flying"
 * "{0}: Flip a coin. If you win the flip, this creature phases out. If you lose the flip, sacrifice this creature."
 */
@CardRegistration(set = "MIR", collectorNumber = "264")
public class FreneticEfreet extends Card {

    public FreneticEfreet() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new FlipCoinWinEffect(new PhaseOutSelfEffect(), new SacrificeSelfEffect())),
                "{0}: Flip a coin. If you win the flip, this creature phases out. "
                        + "If you lose the flip, sacrifice this creature."));
    }
}
