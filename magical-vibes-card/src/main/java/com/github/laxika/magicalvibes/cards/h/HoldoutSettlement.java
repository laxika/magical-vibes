package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "172")
public class HoldoutSettlement extends Card {

    public HoldoutSettlement() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Tap an untapped creature you control: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate()),
                        new AwardAnyColorManaEffect()),
                "{T}, Tap an untapped creature you control: Add one mana of any color."
        ));
    }
}
