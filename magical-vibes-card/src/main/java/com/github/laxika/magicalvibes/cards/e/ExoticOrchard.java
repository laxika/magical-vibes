package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsLandsCouldProduceEffect;
import com.github.laxika.magicalvibes.model.effect.ManaColorLandScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "142")
@CardRegistration(set = "PC2", collectorNumber = "117")
@CardRegistration(set = "PCA", collectorNumber = "117")
@CardRegistration(set = "MSC", collectorNumber = "241")
@CardRegistration(set = "MSC", collectorNumber = "470")
@CardRegistration(set = "SLD", collectorNumber = "1231")
@CardRegistration(set = "ECC", collectorNumber = "148")
@CardRegistration(set = "TMC", collectorNumber = "66")
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
