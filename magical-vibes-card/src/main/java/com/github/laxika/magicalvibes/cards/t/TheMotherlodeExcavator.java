package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "62")
@CardRegistration(set = "PIP", collectorNumber = "389")
@CardRegistration(set = "PIP", collectorNumber = "590")
@CardRegistration(set = "PIP", collectorNumber = "917")
public class TheMotherlodeExcavator extends Card {

    private static final PermanentPredicate NONBASIC_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
    ));

    private static final PermanentPredicate DEFENDING_PLAYER_NONFLYING_CREATURE =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledByDefendingPlayerPredicate(),
                    new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
            ));

    public TheMotherlodeExcavator() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent")).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnergyCountersEffect(new PermanentCount(NONBASIC_LAND, CountScope.TARGET_PLAYER)));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        NONBASIC_LAND,
                        new PermanentControlledByDefendingPlayerPredicate()
                )),
                "Target must be a nonbasic land defending player controls")).addEffect(
                EffectSlot.ON_ATTACK,
                new MayEffect(
                        ConditionalEffect.unless(new ControllerEnergyAtLeast(4),
                                SequenceEffect.of(
                                        new EnergyCountersEffect(-4),
                                        new DestroyTargetPermanentEffect(),
                                        new CantBlockThisTurnEffect(
                                                TapUntapScope.ALL_CREATURES,
                                                DEFENDING_PLAYER_NONFLYING_CREATURE))),
                        "Pay {E}{E}{E}{E} to destroy target nonbasic land and stop creatures without flying from blocking?"));
    }
}
