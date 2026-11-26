package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "114")
public class WandOfIth extends Card {

    private static final CardPredicate LAND_CARD = new CardTypePredicate(CardType.LAND);
    private static final CardPredicate NONLAND_CARD = new CardNotPredicate(LAND_CARD);

    public WandOfIth() {
        // {3}, {T}: Target player reveals a card at random from their hand. If it's a land card,
        // that player discards it unless they pay 1 life. Otherwise, they discard it unless they
        // pay life equal to its mana value. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        RevealCardsFromHandEffect.atRandom(Subjects.targetPlayer(), 1),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, LAND_CARD),
                                UnlessPaysEffect.unlessPaysLife(Subjects.targetPlayer(), 1,
                                        new DiscardCardsEffect(Subjects.revealedThisWay()))),
                        new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, NONLAND_CARD),
                                UnlessPaysEffect.unlessPays(Subjects.targetPlayer(),
                                        new PayLifeCost(AmountThisWay.revealedManaValue()),
                                        new DiscardCardsEffect(Subjects.revealedThisWay())))),
                "{3}, {T}: Target player reveals a card at random from their hand. If it's a land card, "
                        + "that player discards it unless they pay 1 life. If it isn't a land card, "
                        + "the player discards it unless they pay life equal to its mana value. "
                        + "Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
