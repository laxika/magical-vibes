package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "C16", collectorNumber = "20")
public class RunehornHellkite extends Card {

    public RunehornHellkite() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        SequenceEffect.of(
                                new DiscardHandEffect(DiscardRecipient.EACH_PLAYER),
                                new EachPlayerDrawsCardEffect(7))
                ),
                "{5}{R}, Exile this card from your graveyard: Each player discards their hand, then draws seven cards."
        ));
    }
}
