package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "726")
@CardRegistration(set = "CMM", collectorNumber = "758")
public class SparkshaperVisionary extends Card {

    public SparkshaperVisionary() {
        PermanentPredicateTargetFilter planeswalkerYouControl = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a planeswalker you control"
        );

        target(planeswalkerYouControl, 0, 99)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new AnimatePermanentsEffect(
                                3, 3, List.of(CardSubtype.BIRD),
                                Set.of(Keyword.FLYING, Keyword.HEXPROOF), CardColor.BLUE,
                                Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN
                        ))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new SetCardTypesUntilEndOfTurnEffect(Set.of(CardType.CREATURE), GrantScope.TARGET))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new GrantEffectToTargetEffect(
                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new ScryEffect(1)),
                                EffectDuration.UNTIL_END_OF_TURN
                        ));
    }
}
