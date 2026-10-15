package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "17")
public class FemerefHealer extends Card {

    public FemerefHealer() {
        // {T}: Prevent the next 1 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(new Fixed(1))),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
