package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "169")
public class CorruptedCrossroads extends Card {

    public CorruptedCrossroads() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast a spell with devoid.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardAnyColorManaEffect(1, ManaSpendRestriction.DEVOID_SPELL)),
                "{T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast a spell with devoid."
        ));
    }
}
