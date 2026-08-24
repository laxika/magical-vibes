package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "139")
public class SaruliCaretaker extends Card {

    public SaruliCaretaker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate(), true),
                        new AwardAnyColorManaEffect()
                ),
                "{T}, Tap an untapped creature you control: Add one mana of any color."
        ));
    }
}
