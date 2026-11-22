package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "152")
public class ThoughtDissector extends Card {

    public ThoughtDissector() {
        var artifactCard = new CardTypePredicate(CardType.ARTIFACT);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new RevealCardsFromTopUntilEffect(Subjects.targetPlayer(), artifactCard)
                                .orUntilRevealed(new XValue()),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(artifactCard)).underControlOf(Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, artifactCard),
                                new SacrificeSelfEffect()),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(artifactCard))),
                "{X}, {T}: Target opponent reveals cards from the top of their library until an artifact card or X cards are revealed, whichever comes first. If an artifact card is revealed this way, put it onto the battlefield under your control and sacrifice this artifact. Put the rest of the revealed cards into that player's graveyard.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
