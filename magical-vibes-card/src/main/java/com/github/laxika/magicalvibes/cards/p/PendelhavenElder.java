package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "209")
public class PendelhavenElder extends Card {

    public PendelhavenElder() {
        var oneOne = new PermanentAllOfPredicate(List.of(
                new PermanentPowerAtLeastPredicate(1),
                new PermanentPowerAtMostPredicate(1),
                new PermanentToughnessAtLeastPredicate(1),
                new PermanentToughnessAtMostPredicate(1)
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostAllOwnCreaturesEffect(1, 2, oneOne)),
                "{T}: Each 1/1 creature you control gets +1/+2 until end of turn."
        ));
    }
}
