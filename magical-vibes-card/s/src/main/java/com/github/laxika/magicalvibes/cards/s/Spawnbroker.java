package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "65")
public class Spawnbroker extends Card {

    public Spawnbroker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ExchangeControlOfTargetPermanentsEffect.forControlledTargetsWithPowerRestriction(
                        new PermanentIsCreaturePredicate()),
                "You may exchange control of target creature you control and target creature an opponent controls with power less than or equal to that creature's power."));
    }
}
