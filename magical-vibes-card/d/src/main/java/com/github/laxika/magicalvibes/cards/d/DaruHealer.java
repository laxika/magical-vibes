package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "19")
public class DaruHealer extends Card {

    public DaruHealer() {
        addMorph("{W}");
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{T}: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
