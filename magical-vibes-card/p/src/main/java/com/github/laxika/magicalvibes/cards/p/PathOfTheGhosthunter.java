package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "18")
@CardRegistration(set = "MOC", collectorNumber = "105")
public class PathOfTheGhosthunter extends Card {

    public static final String PLANESWALK = "Planeswalk";
    public static final String CHAOS = "Chaos";

    public PathOfTheGhosthunter() {
        addSpellEffect(new CreateTokenEffect(new XValue(), "Spirit", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
        addSpellEffect(VoteEffect.startingWithYou(PLANESWALK, CHAOS));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(PLANESWALK), new PlaneswalkEffect()));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(CHAOS), new ChaosEnsuesEffect()));
    }
}
