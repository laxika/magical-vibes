package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "110")
public class MartyrsTomb extends Card {

    public MartyrsTomb() {
        // Pay 2 life: Prevent the next 1 damage that would be dealt to target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(2), PreventDamageEffect.nextToTargetCreature(1)),
                "Pay 2 life: Prevent the next 1 damage that would be dealt to target creature this turn.",
                TargetFilters.creature()
        ));
    }
}
