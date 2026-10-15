package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "85")
public class DeadOfWinter extends Card {

    public DeadOfWinter() {
        // All nonsnow creatures get -X/-X until end of turn, where X is the number of snow
        // permanents you control.
        PermanentCount snowPermanentsYouControl = new PermanentCount(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(snowPermanentsYouControl, -1),
                new Scaled(snowPermanentsYouControl, -1),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.SNOW))))));
    }
}
