package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "127")
public class UnforgivingOne extends Card {

    public UnforgivingOne() {
        PermanentCount modifiedCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsModifiedPredicate())),
                CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ATTACK, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .dynamicMaxManaValue(modifiedCreatures)
                .build());
    }
}
