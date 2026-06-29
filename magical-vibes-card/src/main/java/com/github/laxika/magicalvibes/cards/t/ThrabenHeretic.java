package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "26")
public class ThrabenHeretic extends Card {

    public ThrabenHeretic() {
        // {T}: Exile target creature card from a graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTargetCardFromGraveyardEffect(CardType.CREATURE)),
                "{T}: Exile target creature card from a graveyard."
        ));
    }
}
