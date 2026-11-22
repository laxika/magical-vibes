package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "155")
@CardRegistration(set = "5ED", collectorNumber = "190")
@CardRegistration(set = "6ED", collectorNumber = "151")
@CardRegistration(set = "7ED", collectorNumber = "156")
@CardRegistration(set = "DRK", collectorNumber = "51")
public class RagMan extends Card {

    public RagMan() {
        // {B}{B}{B}, {T}: Target opponent reveals their hand and discards a creature card at random.
        // Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{B}{B}{B}",
                List.of(RevealCardsFromHandEffect.hand(Subjects.targetPlayer()),
                        ChooseCardEffect.atRandom(Subjects.revealedThisWay(new CardTypePredicate(CardType.CREATURE)), "a creature card"),
                        new DiscardCardsEffect(Subjects.cardsThisWay(StepKind.CHOOSE))),
                "{B}{B}{B}, {T}: Target opponent reveals their hand and discards a creature card at random. "
                        + "Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
