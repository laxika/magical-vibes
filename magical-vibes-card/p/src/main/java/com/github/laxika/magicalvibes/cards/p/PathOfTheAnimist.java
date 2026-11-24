package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "MOC", collectorNumber = "38")
@CardRegistration(set = "MOC", collectorNumber = "125")
public class PathOfTheAnimist extends Card {

    public static final String PLANESWALK = "Planeswalk";
    public static final String CHAOS = "Chaos";

    public PathOfTheAnimist() {
        addSpellEffect(new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addSpellEffect(VoteEffect.startingWithYou(PLANESWALK, CHAOS));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(PLANESWALK), new PlaneswalkEffect()));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(CHAOS), new ChaosEnsuesEffect()));
    }
}
