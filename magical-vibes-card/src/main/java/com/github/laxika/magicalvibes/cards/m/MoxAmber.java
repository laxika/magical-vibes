package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsAmongControlledEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "224")
public class MoxAmber extends Card {

    public MoxAmber() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsAmongControlledEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                                        new PermanentIsCreaturePredicate()
                                )),
                                new PermanentIsPlaneswalkerPredicate()
                        ))
                )),
                "{T}: Add one mana of any color among legendary creatures and planeswalkers you control."
        ));
    }
}
