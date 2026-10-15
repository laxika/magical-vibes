package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "9")
public class MilitantMonk extends Card {

    public MilitantMonk() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
