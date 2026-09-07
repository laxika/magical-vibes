package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.TapSourceThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "98")
public class TheFireNationDrill extends Card {

    public TheFireNationDrill() {
        PermanentPredicate creaturePowerAtMostFour = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(4)
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new TapSourceThenEffect(new DestroyTargetPermanentEffect(creaturePowerAtMostFour)),
                "Tap The Fire Nation Drill?"
        ));

        PermanentPredicate opponentPermanent = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveKeywordEffect(Keyword.HEXPROOF, GrantScope.ALL_PERMANENTS, opponentPermanent),
                        new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_PERMANENTS, opponentPermanent)
                ),
                "{1}: Permanents your opponents control lose hexproof and indestructible until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
