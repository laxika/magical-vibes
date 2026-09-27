package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "43")
@CardRegistration(set = "TDC", collectorNumber = "83")
public class BecomeTheAvalanche extends Card {

    public BecomeTheAvalanche() {
        PermanentAllOfPredicate qualifyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));

        addEffect(EffectSlot.SPELL, new DrawCardEffect(new PermanentCount(
                qualifyingCreature, CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(
                new CardsInHand(CountScope.CONTROLLER),
                new CardsInHand(CountScope.CONTROLLER)));
    }
}
