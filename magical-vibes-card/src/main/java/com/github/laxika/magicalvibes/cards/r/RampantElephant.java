package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "28")
public class RampantElephant extends Card {

    public RampantElephant() {
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new MustBlockSourceEffect(null)),
                "{G}: Target creature blocks Rampant Elephant this turn if able."));
    }
}
