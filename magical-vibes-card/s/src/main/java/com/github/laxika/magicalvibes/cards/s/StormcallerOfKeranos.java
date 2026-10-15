package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "112")
public class StormcallerOfKeranos extends Card {

    public StormcallerOfKeranos() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new ScryEffect(1)),
                "{1}{U}: Scry 1."
        ));
    }
}
