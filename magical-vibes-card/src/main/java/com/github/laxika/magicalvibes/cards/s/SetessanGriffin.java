package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "30")
public class SetessanGriffin extends Card {

    public SetessanGriffin() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{2}{G}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.", 1));
    }
}
