package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "156")
@CardRegistration(set = "MMQ", collectorNumber = "152")
public class PrimevalShambler extends Card {

    public PrimevalShambler() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)), "{B}: Primeval Shambler gets +1/+1 until end of turn."));
    }
}
