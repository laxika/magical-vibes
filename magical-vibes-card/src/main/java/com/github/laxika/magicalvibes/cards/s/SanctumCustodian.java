package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "42")
@CardRegistration(set = "BRB", collectorNumber = "62")
public class SanctumCustodian extends Card {

    public SanctumCustodian() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(2)),
                "{T}: Prevent the next 2 damage that would be dealt to any target this turn."
        ));
    }
}
