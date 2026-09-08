package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "122")
public class RazorlashTransmogrant extends Card {

    public RazorlashTransmogrant() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        var nonbasicLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))));
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}",
                List.of(
                        new ReduceActivationCostEffect(new FixedIfCondition(
                                new OpponentControlsPermanentCount(4, nonbasicLand), 4, 0)),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .plusOneCounterCount(1)
                                .build()),
                "{4}{B}{B}: Return this card from your graveyard to the battlefield with a +1/+1 counter on it. "
                        + "This ability costs {4} less to activate if an opponent controls four or more nonbasic lands."
        ));
    }
}
