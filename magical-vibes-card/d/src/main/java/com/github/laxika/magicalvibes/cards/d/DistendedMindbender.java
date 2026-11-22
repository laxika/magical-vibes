package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "3")
@CardRegistration(set = "EMN", collectorNumber = "3")
@CardRegistration(set = "SIR", collectorNumber = "3")
public class DistendedMindbender extends Card {

    public DistendedMindbender() {
        // Emerge {5}{B}{B} — sacrifice a creature and pay the emerge cost reduced by that
        // creature's mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{5}{B}{B}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        // When you cast this spell, target opponent reveals their hand. You choose from it a
        // nonland card with mana value 3 or less and a card with mana value 4 or greater. That
        // player discards those cards.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.youCastThisSpell(),
                RevealCardsFromHandEffect.hand(Subjects.targetPlayer()),
                new ChooseCardEffect(Subjects.revealedThisWay(), "a card for each", true, new Fixed(1), false, List.of(
                        new ChooseCardEffect.Each(new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                new CardMaxManaValuePredicate(3))),
                                "a nonland card with mana value 3 or less to discard"),
                        new ChooseCardEffect.Each(new CardMinManaValuePredicate(4),
                                "a card with mana value 4 or greater to discard"))),
                new DiscardCardsEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
