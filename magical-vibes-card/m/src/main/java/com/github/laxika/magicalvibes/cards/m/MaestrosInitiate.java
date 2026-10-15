package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "86")
public class MaestrosInitiate extends Card {

    public MaestrosInitiate() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U/R}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "{4}{U/R}, Exile this card from your graveyard: Draw two cards, then discard a card."
        ));
    }
}
