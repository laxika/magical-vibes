package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "169")
@CardRegistration(set = "INV", collectorNumber = "142")
public class CrownOfFlames extends Card {

    public CrownOfFlames() {
        target(TargetFilters.creature());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(1), new Fixed(0))),
                "{R}: Enchanted creature gets +1/+0 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(ReturnToHandEffect.self()),
                "{R}: Return this Aura to its owner's hand."
        ));
    }
}
