package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "49")
public class ViralDrake extends Card {

    public ViralDrake() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ProliferateEffect()),
                "{3}{U}: Proliferate."
        ));
    }
}
