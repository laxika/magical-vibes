package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfTypeLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "278")
@CardRegistration(set = "TMP", collectorNumber = "322")
public class ReflectingPool extends Card {

    public ReflectingPool() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfTypeLandsCouldProduceEffect(
                        ManaColorLandScope.CONTROLLER, new PermanentIsLandPredicate())),
                "{T}: Add one mana of any type that a land you control could produce."
        ));
    }
}
