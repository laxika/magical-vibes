package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "4")
public class DroningBureaucrats extends Card {

    public DroningBureaucrats() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new LockMatchingPermanentsEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentManaValueEqualsXPredicate())),
                        true, true, false, EffectDuration.UNTIL_END_OF_TURN)),
                "{X}, {T}: Each creature with mana value X can't attack or block this turn."));
    }
}
