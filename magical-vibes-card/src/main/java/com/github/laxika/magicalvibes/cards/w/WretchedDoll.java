package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "111")
public class WretchedDoll extends Card {

    public WretchedDoll() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new SurveilEffect(1)),
                "{B}, {T}: Surveil 1."
        ));
    }
}
