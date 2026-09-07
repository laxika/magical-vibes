package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsDownToFewestEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToFewestEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "38")
public class RestoreBalance extends Card {

    public RestoreBalance() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(),
                "Suspend 6\u2014{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(6));
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesDownToFewestEffect(new PermanentIsLandPredicate()));
        addEffect(EffectSlot.SPELL, new EachPlayerDiscardsDownToFewestEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesDownToFewestEffect(new PermanentIsCreaturePredicate()));
    }
}
