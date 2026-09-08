package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "116")
@CardRegistration(set = "BRB", collectorNumber = "52")
public class PlatedRootwalla extends Card {

    public PlatedRootwalla() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new BoostSelfEffect(3, 3)),
                "{2}{G}: This creature gets +3/+3 until end of turn. Activate only once each turn.", 1));
    }
}
