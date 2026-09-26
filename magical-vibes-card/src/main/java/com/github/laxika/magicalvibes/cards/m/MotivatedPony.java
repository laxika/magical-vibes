package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "LTC", collectorNumber = "42")
@CardRegistration(set = "LTC", collectorNumber = "125")
public class MotivatedPony extends Card {

    public MotivatedPony() {
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new BoostAllOwnCreaturesEffect(1, 1, attacking),
                ConditionalEffect.unless(
                        new AnotherPermanentEnteredThisTurn(new CardSubtypePredicate(CardSubtype.FOOD)),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, attacking),
                                new BoostAllOwnCreaturesEffect(2, 2, attacking)))));
    }
}
