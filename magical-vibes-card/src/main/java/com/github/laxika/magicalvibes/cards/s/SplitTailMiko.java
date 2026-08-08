package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "23")
public class SplitTailMiko extends Card {

    public SplitTailMiko() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(PreventDamageEffect.nextToTarget(2)),
                "{W}, {T}: Prevent the next 2 damage that would be dealt to any target this turn."
        ));
    }
}
