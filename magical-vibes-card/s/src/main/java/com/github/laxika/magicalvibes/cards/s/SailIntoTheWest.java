package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "LTC", collectorNumber = "68")
@CardRegistration(set = "LTC", collectorNumber = "149")
@CardRegistration(set = "LTC", collectorNumber = "475")
public class SailIntoTheWest extends Card {

    public static final String RETURN = "Return";
    public static final String EMBARK = "Embark";

    public SailIntoTheWest() {
        addSpellEffect(VoteEffect.startingWithYou(RETURN, EMBARK));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(RETURN), SequenceEffect.of(
                new EachPlayerReturnsCardsFromGraveyardToHandEffect(2, new CardTruePredicate()),
                new ExileSpellEffect())));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(EMBARK),
                new EachPlayerMayDiscardHandThenDrawEffect(7)));
    }
}
