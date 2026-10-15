package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "129")
public class EyeOfYawgmoth extends Card {

    public EyeOfYawgmoth() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeCreatureCost(false, true),
                        new LookAtTopCardsEffect(
                                new XValue(), new Fixed(1), null,
                                LookDestination.EXILE, true)
                ),
                "{3}, {T}, Sacrifice a creature: Reveal a number of cards from the top of your library "
                        + "equal to the sacrificed creature's power. Put one into your hand and exile the rest."
        ));
    }
}
