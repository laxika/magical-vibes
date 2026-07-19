package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "142")
public class ExoticOrchard extends Card {

    public ExoticOrchard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsLandsCouldProduceEffect(
                        ManaColorLandScope.OPPONENTS, new PermanentIsLandPredicate())),
                "{T}: Add one mana of any color that a land an opponent controls could produce."
        ));
    }
}
