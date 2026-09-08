package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "49")
public class RuneSealedWall extends Card {

    public RuneSealedWall() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new SurveilEffect(1)),
                "{T}: Surveil 1."));
    }
}
