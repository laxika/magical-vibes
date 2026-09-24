package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "159")
public class ConiferWurm extends Card {

    public ConiferWurm() {
        PermanentCount snowPermanentsYouControl = new PermanentCount(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostSelfEffect(snowPermanentsYouControl, snowPermanentsYouControl)),
                "{3}{G}: This creature gets +X/+X until end of turn, where X is the number of snow permanents you control."
        ));
    }
}
