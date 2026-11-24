package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "28")
@CardRegistration(set = "MOC", collectorNumber = "115")
public class PathOfTheSchemer extends Card {

    public static final String PLANESWALK = "Planeswalk";
    public static final String CHAOS = "Chaos";

    public PathOfTheSchemer() {
        // Each player mills two cards.
        addSpellEffect(new MillEffect(2, Subjects.eachPlayer()));
        // Then put a creature card from a graveyard onto the battlefield under your control (not optional).
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .mandatory(true)
                .battlefieldEffectGrants(List.of(
                        new GrantCardTypeEffect(CardType.ARTIFACT, Subjects.target())))
                .build());
        addSpellEffect(VoteEffect.startingWithYou(PLANESWALK, CHAOS));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotes(PLANESWALK), new PlaneswalkEffect()));
        addSpellEffect(new ConditionalEffect(VoteResult.moreVotesOrTied(CHAOS), new ChaosEnsuesEffect()));
    }
}
