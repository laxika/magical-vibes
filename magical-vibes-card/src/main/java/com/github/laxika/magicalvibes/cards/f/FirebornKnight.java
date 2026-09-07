package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "210")
public class FirebornKnight extends Card {

    public FirebornKnight() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R/W}{R/W}{R/W}{R/W}",
                List.of(new BoostSelfEffect(1, 1)),
                "{R/W}{R/W}{R/W}{R/W}: This creature gets +1/+1 until end of turn."
        ));
    }
}
