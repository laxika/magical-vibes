package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "32")
public class VladimirAndGodfrey extends Card {

    public VladimirAndGodfrey() {
        var oneOneCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(1),
                new PermanentPowerAtMostPredicate(1),
                new PermanentToughnessAtLeastPredicate(1),
                new PermanentToughnessAtMostPredicate(1)
        ));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build(),
                        new PerpetuallyBoostSourceEffect(1, 1)
                ),
                "Rejuvenation — {2}{W}: Return Vladimir and Godfrey from your graveyard to the battlefield tapped. "
                        + "It perpetually gets +1/+1. Activate only if you control a 1/1 creature."
        ).withActivationCondition(
                new ControlsPermanentCount(1, oneOneCreature),
                "Activate only if you control a 1/1 creature."
        ));
    }
}
