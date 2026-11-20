package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "156")
@CardRegistration(set = "LCI", collectorNumber = "295")
public class IntiSeneschalOfTheSun extends Card {

    public IntiSeneschalOfTheSun() {
        addTrigger(Triggers.youAttack(), new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        SequenceEffect.of(
                                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentIsAttackingPredicate()),
                                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target())),
                        "a card"),
                "Discard a card to put a +1/+1 counter on target attacking creature and give it trample?"));

        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                new ExileTopCardMayPlayThisTurnEffect(1, false)
                        .until(Duration.UNTIL_YOUR_NEXT_END_STEP));
    }
}
