package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MOC", collectorNumber = "23")
@CardRegistration(set = "MOC", collectorNumber = "110")
public class PathOfTheEnigma extends Card {

    public static final String PLANESWALK = "Planeswalk";
    public static final String CHAOS = "Chaos";

    public PathOfTheEnigma() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new DrawCardEffect(4, Subjects.targetPlayer()));
        addSpellEffect(VoteEffect.startingWithYou(PLANESWALK, CHAOS));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(PLANESWALK), new PlaneswalkEffect()));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(CHAOS), new ChaosEnsuesEffect()));
    }
}
