package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "186")
public class GenePollinator extends Card {

    public GenePollinator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentTruePredicate(), true),
                        new AwardAnyColorManaEffect()),
                "{T}, Tap an untapped permanent you control: Add one mana of any color."
        ));
    }
}
