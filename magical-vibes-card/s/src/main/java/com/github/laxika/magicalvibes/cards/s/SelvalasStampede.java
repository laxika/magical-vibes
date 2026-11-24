package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TDC", collectorNumber = "269")
public class SelvalasStampede extends Card {

    public static final String WILD = "Wild";
    public static final String FREE = "Free";

    public SelvalasStampede() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addSpellEffect(VoteEffect.startingWithYou(WILD, FREE));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(WILD), SequenceEffect.of(
                new RevealCardsFromTopUntilEffect(Subjects.you(), creature, new VotesFor(WILD)),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(creature)),
                new ShuffleLibraryEffect(false))));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(FREE), new PutUpToCardsFromHandOntoBattlefieldEffect(
                new CardIsPermanentPredicate(), "permanent", new VotesFor(FREE))));
    }
}
