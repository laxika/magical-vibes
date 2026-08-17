package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "127")
public class DevkarinDissident extends Card {

    public DevkarinDissident() {
        addActivatedAbility(new ActivatedAbility(false, "{4}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{4}{G}: This creature gets +2/+2 until end of turn."));
    }
}
