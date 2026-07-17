package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "297")
@CardRegistration(set = "5ED", collectorNumber = "369")
public class FellwarStone extends Card {

    public FellwarStone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsLandsCouldProduceEffect(
                        ManaColorLandScope.OPPONENTS, new PermanentIsLandPredicate())),
                "{T}: Add one mana of any color that a land an opponent controls could produce."
        ));
    }
}
