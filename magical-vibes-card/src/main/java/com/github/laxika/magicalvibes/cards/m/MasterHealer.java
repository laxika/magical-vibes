package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "30")
@CardRegistration(set = "9ED", collectorNumber = "28")
public class MasterHealer extends Card {

    public MasterHealer() {
        // {T}: Prevent the next 4 damage that would be dealt to any target this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PreventDamageToTargetEffect(4)),
                "{T}: Prevent the next 4 damage that would be dealt to any target this turn."
        ));
    }
}
