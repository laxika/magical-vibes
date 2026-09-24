package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
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
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new AnimatePermanentsEffect(
                                3, 3, List.of(CardSubtype.BIRD),
                                Set.of(Keyword.FLYING, Keyword.HEXPROOF), CardColor.BLUE,
                                Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN
                        ))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new SetCardTypesUntilEndOfTurnEffect(Set.of(CardType.CREATURE), GrantScope.TARGET))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new ScryEffect(1),
                                EffectDuration.UNTIL_END_OF_TURN
                        ));
    }
}
