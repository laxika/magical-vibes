package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "255")
@CardRegistration(set = "C21", collectorNumber = "223")
public class MagisterOfWorth extends Card {

    public static final String GRACE = "Grace";
    public static final String CONDEMNATION = "Condemnation";

    public MagisterOfWorth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                VoteEffect.startingWithYou(GRACE, CONDEMNATION),
                new ConditionalEffect(VoteResult.moreVotes(GRACE),
                        new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                                Integer.MAX_VALUE, new CardTypePredicate(CardType.CREATURE))),
                new ConditionalEffect(VoteResult.moreVotesOrTied(CONDEMNATION),
                        DestroyTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())))))));
    }
}
