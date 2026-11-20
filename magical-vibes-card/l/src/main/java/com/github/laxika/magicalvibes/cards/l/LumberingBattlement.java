package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "15")
public class LumberingBattlement extends Card {

    public LumberingBattlement() {
        // When this creature enters, exile any number of other nontoken creatures you control until
        // it leaves the battlefield.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileAnyNumberOfPermanentsUntilSourceLeavesEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))));
        // This creature gets +2/+2 for each card exiled with it.
        addStaticEffect(new BoostSelfEffect(
                new Scaled(new CardsExiledWithSource(), 2),
                new Scaled(new CardsExiledWithSource(), 2)));
    }
}
