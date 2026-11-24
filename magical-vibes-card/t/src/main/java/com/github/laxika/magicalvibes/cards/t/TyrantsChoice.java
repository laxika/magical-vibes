package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "VMA", collectorNumber = "143")
public class TyrantsChoice extends Card {

    public static final String DEATH = "Death";
    public static final String TORTURE = "Torture";

    public TyrantsChoice() {
        addSpellEffect(VoteEffect.startingWithYou(DEATH, TORTURE));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(DEATH), new SacrificePermanentsEffect(1,
                new PermanentIsCreaturePredicate(), Subjects.eachOpponent()).withSimultaneousChoices()));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(TORTURE),
                new LoseLifeEffect(4, Subjects.eachOpponent())));
    }
}
