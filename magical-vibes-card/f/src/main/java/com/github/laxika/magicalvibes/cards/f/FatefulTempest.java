package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;

@CardRegistration(set = "SOC", collectorNumber = "31")
@CardRegistration(set = "SOC", collectorNumber = "79")
public class FatefulTempest extends Card {

    public static final String PAST = "Past";
    public static final String PRESENT = "Present";

    public FatefulTempest() {
        addSpellEffect(VoteEffect.startingWithYou(PAST, PRESENT));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(PAST), SequenceEffect.of(
                new MillEffect(new VotesFor(PAST), Subjects.you()),
                DealDamageEffect.to(Subjects.eachOpponent(), AmountThisWay.milledManaValue()))));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(PRESENT),
                new ExileTopCardMayPlayThisTurnEffect(new VotesFor(PRESENT), false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
    }
}
