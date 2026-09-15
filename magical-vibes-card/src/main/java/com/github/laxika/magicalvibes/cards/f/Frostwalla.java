package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "165")
public class Frostwalla extends Card {

    public Frostwalla() {
        addActivatedAbility(new ActivatedAbility(false, "{S}", List.of(new BoostSelfEffect(2, 2)),
                "{S}: This creature gets +2/+2 until end of turn. Activate only once each turn.", 1));
    }
}
