package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "96")
public class Detritivore extends Card {

    public Detritivore() {
        CardsInGraveyard nonbasicLandCardsInOpponentsGraveyards = new CardsInGraveyard(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardNotPredicate(new CardSupertypePredicate(CardSupertype.BASIC))
                )),
                CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                nonbasicLandCardsInOpponentsGraveyards,
                nonbasicLandCardsInOpponentsGraveyards));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                )),
                "Target must be a nonbasic land"
        )).addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
                new DestroyTargetPermanentEffect(false));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{3}{R}",
                List.of(),
                "Suspend X—{X}{3}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHandX());
    }
}
