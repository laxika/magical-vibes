package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "GN3", collectorNumber = "39")
public class SplitDecision extends Card {

    public static final String DENIAL = "Denial";
    public static final String DUPLICATION = "Duplication";

    public SplitDecision() {
        addSpellEffect(VoteEffect.startingWithYou(DENIAL, DUPLICATION));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ))
                .addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(DENIAL), new CounterSpellEffect()))
                .addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(DUPLICATION),
                        new CopySpellEffect()));
    }
}
