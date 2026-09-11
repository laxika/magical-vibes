package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "97")
public class DemonsDisciple extends Card {

    public DemonsDisciple() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsEffect(1, new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())), SacrificeRecipient.EACH_PLAYER)
                        .withSimultaneousChoices());
    }
}
