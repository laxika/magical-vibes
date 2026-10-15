package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "225")
public class PearlShard extends Card {

    public PearlShard() {
        // {3}, {T} or {W}, {T}: Prevent the next 2 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(PreventDamageEffect.nextToTarget(2)),
                "{3}, {T}: Prevent the next 2 damage that would be dealt to any target this turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(PreventDamageEffect.nextToTarget(2)),
                "{W}, {T}: Prevent the next 2 damage that would be dealt to any target this turn."
        ));
    }
}
