package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "270")
public class SeleniaDarkAngel extends Card {

    public SeleniaDarkAngel() {
        // Pay 2 life: Return Selenia, Dark Angel to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2), ReturnToHandEffect.self()),
                "Pay 2 life: Return Selenia, Dark Angel to its owner's hand."));
    }
}
