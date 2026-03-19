package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "238")
public class CabalStronghold extends Card {

    public CabalStronghold() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {3}, {T}: Add {B} for each basic Swamp you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new AddManaPerControlledPermanentEffect(
                        ManaColor.BLACK,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                new PermanentHasSupertypePredicate(CardSupertype.BASIC)
                        )),
                        "basic Swamps"
                )),
                "{3}, {T}: Add {B} for each basic Swamp you control."
        ));
    }
}
