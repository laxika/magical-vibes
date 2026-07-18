package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "310")
@CardRegistration(set = "5ED", collectorNumber = "359")
public class CoralHelm extends Card {

    public CoralHelm() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new DiscardRandomCardCost(),
                        new BoostTargetCreatureEffect(2, 2)
                ),
                "{3}, Discard a card at random: Target creature gets +2/+2 until end of turn."
        ));
    }
}
