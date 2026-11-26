package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "66")
@CardRegistration(set = "SIR", collectorNumber = "75")
@CardRegistration(set = "2X2", collectorNumber = "52")
@CardRegistration(set = "PIO", collectorNumber = "59")
public class IngeniousSkaab extends Card {

    public IngeniousSkaab() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(BoostEffect.self(1, -1)),
                "{U}: This creature gets +1/-1 until end of turn."));
    }
}
