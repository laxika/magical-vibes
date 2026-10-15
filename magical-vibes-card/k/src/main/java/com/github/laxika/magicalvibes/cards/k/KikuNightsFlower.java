package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "121")
public class KikuNightsFlower extends Card {

    public KikuNightsFlower() {
        addActivatedAbility(new ActivatedAbility(true, "{2}{B}{B}",
                List.of(new TargetCreatureDealsPowerDamageToSelfEffect()),
                "{2}{B}{B}, {T}: Target creature deals damage to itself equal to its power."));
    }
}
