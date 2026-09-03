package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "248")
public class BirchloreRangers extends Card {

    public BirchloreRangers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                        new AwardAnyColorManaEffect()),
                "Tap two untapped Elves you control: Add one mana of any color."
        ));
        addMorph("{G}");
    }
}
