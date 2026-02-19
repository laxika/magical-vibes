package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "65")
public class AmbassadorLaquatus extends Card {

    public AmbassadorLaquatus() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new MillTargetPlayerEffect(3)), true, "{3}: Target player mills three cards."));
    }
}
