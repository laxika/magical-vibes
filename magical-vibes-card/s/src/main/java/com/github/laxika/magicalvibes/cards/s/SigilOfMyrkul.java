package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "168")
public class SigilOfMyrkul extends Card {

    public SigilOfMyrkul() {
        PermanentPredicate creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MillControllerThenIfConditionEffect(
                        1,
                        new GraveyardCardThreshold(4, new CardTypePredicate(CardType.CREATURE)),
                        SequenceEffect.of(
                                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1, creatureYouControl),
                                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET,
                                        creatureYouControl))));
    }
}
