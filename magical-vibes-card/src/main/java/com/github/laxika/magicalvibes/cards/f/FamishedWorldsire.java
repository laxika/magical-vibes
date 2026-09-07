package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "EOE", collectorNumber = "182")
public class FamishedWorldsire extends Card {

    public FamishedWorldsire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsAsEntersForCountersEffect(new PermanentIsLandPredicate(), 3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayPutAnyNumberMatchingOntoBattlefieldTappedShuffleRest(
                        new SourcePower(), new CardTypePredicate(CardType.LAND)));
    }
}
