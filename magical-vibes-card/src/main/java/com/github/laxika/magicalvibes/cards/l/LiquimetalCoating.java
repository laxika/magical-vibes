package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "171")
public class LiquimetalCoating extends Card {

    public LiquimetalCoating() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT)),
                "{T}: Target permanent becomes an artifact in addition to its other types until end of turn."
        ));
    }
}
