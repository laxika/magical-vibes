package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "88")
public class IllicitMasquerade extends Card {

    public IllicitMasquerade() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.IMPOSTOR, 1, new PermanentIsCreaturePredicate()));

        var otherCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardIsSelfPredicate())));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.IMPOSTOR),
                        SequenceEffect.of(
                                new ExileTriggeringCreatureFromGraveyardEffect(),
                                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                                        otherCreature, 1, false, false))));
    }
}
