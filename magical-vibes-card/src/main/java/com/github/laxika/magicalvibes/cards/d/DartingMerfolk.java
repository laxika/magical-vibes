package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "72")
public class DartingMerfolk extends Card {

    public DartingMerfolk() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(ReturnToHandEffect.self()),
                "{U}: Return this creature to its owner's hand."));
    }
}
