package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "123")
public class FetidHorror extends Card {

    public FetidHorror() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 1)), "{B}: This creature gets +1/+1 until end of turn."));
    }
}
