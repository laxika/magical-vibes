package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "362")
public class Oasis extends Card {

    public Oasis() {
        // {T}: Prevent the next 1 damage that would be dealt to target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(1)),
                "{T}: Prevent the next 1 damage that would be dealt to target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
