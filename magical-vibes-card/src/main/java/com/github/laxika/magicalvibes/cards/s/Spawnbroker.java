package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RAV", collectorNumber = "65")
public class Spawnbroker extends Card {

    public Spawnbroker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                ExchangeControlOfTargetPermanentsEffect.forControlledTargetsWithPowerRestriction(
                        new PermanentIsCreaturePredicate()),
                "You may exchange control of target creature you control and target creature an opponent controls with power less than or equal to that creature's power."));
    }
}
