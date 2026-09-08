package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "231")
public class BoaConstrictor extends Card {

    public BoaConstrictor() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new BoostSelfEffect(3, 3)),
                "{T}: This creature gets +3/+3 until end of turn."));
    }
}
