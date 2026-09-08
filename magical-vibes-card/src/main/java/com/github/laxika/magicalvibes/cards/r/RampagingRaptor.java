package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByDefendingPlayerPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "160")
public class RampagingRaptor extends Card {

    public RampagingRaptor() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentControlledByDefendingPlayerPredicate())),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsBattlePredicate(),
                                new PermanentProtectedByDefendingPlayerPredicate())))),
                "Target must be a planeswalker defending player controls or a battle defending player protects"))
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new DealDamageToTargetPermanentEffect(new EventValue()));
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new BoostSelfEffect(2, 0)),
                "{2}{R}: This creature gets +2/+0 until end of turn."));
    }
}
