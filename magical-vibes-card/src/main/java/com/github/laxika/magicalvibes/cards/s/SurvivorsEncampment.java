package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "184")
public class SurvivorsEncampment extends Card {

    public SurvivorsEncampment() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

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
