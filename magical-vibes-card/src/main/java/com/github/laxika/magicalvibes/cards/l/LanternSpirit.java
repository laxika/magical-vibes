package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "62")
public class LanternSpirit extends Card {

    public LanternSpirit() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(ReturnToHandEffect.self()), "{U}: Return Lantern Spirit to its owner's hand."));
    }
}
