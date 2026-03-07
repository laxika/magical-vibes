package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "139")
public class ImmolatingSouleater extends Card {

    public ImmolatingSouleater() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R/P}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R/P}: Immolating Souleater gets +1/+0 until end of turn."
        ));
    }
}
