package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "176")
public class NagaVitalist extends Card {

    public NagaVitalist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsLandsCouldProduceEffect(
                        ManaColorLandScope.CONTROLLER, new PermanentIsLandPredicate())),
                "{T}: Add one mana of any type that a land you control could produce."
        ));
    }
}
