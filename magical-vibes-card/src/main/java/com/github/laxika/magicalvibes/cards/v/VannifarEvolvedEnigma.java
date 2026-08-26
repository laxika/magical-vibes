package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "241")
public class VannifarEvolvedEnigma extends Card {

    public VannifarEvolvedEnigma() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Cloak a card from your hand",
                        PutCardToBattlefieldEffect.cloakedFromHand()),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each colorless creature you control",
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsColorlessPredicate())))
        ))));
    }
}
