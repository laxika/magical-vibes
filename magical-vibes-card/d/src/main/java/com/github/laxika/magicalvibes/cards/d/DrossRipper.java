package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "106")
public class DrossRipper extends Card {

    public DrossRipper() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(BoostEffect.self(1, 1)),
                "{2}{B}: Dross Ripper gets +1/+1 until end of turn."));
    }
}
