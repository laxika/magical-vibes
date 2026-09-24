package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "A25", collectorNumber = "142")
@CardRegistration(set = "CMM", collectorNumber = "241")
@CardRegistration(set = "CMM", collectorNumber = "544")
@CardRegistration(set = "CMM", collectorNumber = "643")
public class MagusOfTheWheel extends Card {

    public MagusOfTheWheel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        SequenceEffect.of(
                                new DiscardHandEffect(DiscardRecipient.EACH_PLAYER),
                                new EachPlayerDrawsCardEffect(7))
                ),
                "{1}{R}, {T}, Sacrifice this creature: Each player discards their hand, then draws seven cards."
        ));
    }
}
