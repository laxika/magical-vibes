package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "126")
public class HeritageDruid extends Card {

    public HeritageDruid() {
        // Tap three untapped Elves you control: Add {G}{G}{G}.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.ELF)),
                        new AwardManaEffect(ManaColor.GREEN, 3)),
                "Tap three untapped Elves you control: Add {G}{G}{G}."
        ));
    }
}
