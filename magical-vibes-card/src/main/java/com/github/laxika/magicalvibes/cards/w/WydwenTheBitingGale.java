package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "253")
public class WydwenTheBitingGale extends Card {

    public WydwenTheBitingGale() {
        // {U}{B}, Pay 1 life: Return Wydwen, the Biting Gale to its owner's hand.
        addActivatedAbility(new ActivatedAbility(false, "{U}{B}",
                List.of(new PayLifeCost(1), ReturnToHandEffect.self()),
                "{U}{B}, Pay 1 life: Return Wydwen, the Biting Gale to its owner's hand."));
    }
}
