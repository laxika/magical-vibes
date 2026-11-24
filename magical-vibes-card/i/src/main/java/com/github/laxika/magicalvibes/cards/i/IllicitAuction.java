package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.BidLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "190")
@CardRegistration(set = "MIR", collectorNumber = "183")
public class IllicitAuction extends Card {

    public IllicitAuction() {
        target(TargetFilters.creature())
                .addSpellEffect(BidLifeEffect.eachPlayerStartingAt(0, BidLifeEffect.Prize.CONTROL_OF_TARGET))
                .addSpellEffect(new LoseLifeEffect(new AmountThisWay(StepKind.BID),
                        Subjects.playersThisWay(StepKind.BID)))
                .addSpellEffect(new GainControlOfTargetEffect(Duration.PERMANENT)
                        .gainedBy(Subjects.playersThisWay(StepKind.BID)));
    }
}
