package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "30")
public class Disappear extends Card {

    public Disappear() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(ReturnToHandEffect.self(), ReturnToHandEffect.enchanted()),
                "{U}: Return enchanted creature and this Aura to their owners' hands."
        ));
    }
}
