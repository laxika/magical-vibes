package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnRingBearerEffect;
import com.github.laxika.magicalvibes.model.effect.RingTemptsYouEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "3")
@CardRegistration(set = "LTC", collectorNumber = "83")
@CardRegistration(set = "LTC", collectorNumber = "88")
@CardRegistration(set = "LTC", collectorNumber = "462")
public class GaladrielElvenQueen extends Card {

    public static final String DOMINION = "Dominion";
    public static final String GUIDANCE = "Guidance";

    public GaladrielElvenQueen() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnotherPermanentEnteredThisTurn(new CardSubtypePredicate(CardSubtype.ELF))),
                SequenceEffect.of(
                        VoteEffect.startingWithYou(DOMINION, GUIDANCE),
                        new ConditionalEffect(VoteResult.moreVotes(DOMINION), SequenceEffect.of(
                                new RingTemptsYouEffect(),
                                new PutCounterOnRingBearerEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                        new ConditionalEffect(VoteResult.moreVotesOrTied(GUIDANCE), new DrawCardEffect(1))));
    }
}
