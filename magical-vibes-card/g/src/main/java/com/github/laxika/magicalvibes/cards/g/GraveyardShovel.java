package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "225")
public class GraveyardShovel extends Card {

    public GraveyardShovel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new TargetPlayerExilesCardFromGraveyardEffect(2)),
                "{2}, {T}: Target player exiles a card from their graveyard. If it's a creature card, you gain 2 life."
        ));
    }
}
