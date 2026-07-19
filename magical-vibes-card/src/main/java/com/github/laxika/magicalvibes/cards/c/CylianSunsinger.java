package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "80")
public class CylianSunsinger extends Card {

    public CylianSunsinger() {
        // {R}{G}{W}: This creature and each other creature with the same name as it get +3/+3 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{G}{W}",
                List.of(new BoostAllCreaturesEffect(3, 3, new PermanentHasSameNameAsSourcePredicate())),
                "{R}{G}{W}: This creature and each other creature with the same name as it get +3/+3 until end of turn."
        ));
    }
}
