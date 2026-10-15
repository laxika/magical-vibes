package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "10")
public class LoxodonAnchorite extends Card {

    public LoxodonAnchorite() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(2)),
                "{T}: Prevent the next 2 damage that would be dealt to any target this turn."
        ));
    }
}
