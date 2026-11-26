package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "83")
@CardRegistration(set = "DSC", collectorNumber = "153")
public class ProfessorOnyx extends Card {

    public ProfessorOnyx() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> magecraft = List.of(
                new LoseLifeEffect(2, Subjects.eachOpponent()),
                new GainLifeEffect(2));

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new LoseLifeEffect(1, Subjects.you()),
                        LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(3, 1)),
                "+1: You lose 1 life. Look at the top three cards of your library. Put one of them "
                        + "into your hand and the rest into your graveyard."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new SacrificePermanentsEffect(1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate())),
                        Subjects.eachOpponent())),
                "\u22123: Each opponent sacrifices a creature with the greatest power among creatures "
                        + "that player controls."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        opponentsLoseLifeUnlessTheyDiscard(3),
                        opponentsLoseLifeUnlessTheyDiscard(3),
                        opponentsLoseLifeUnlessTheyDiscard(3),
                        opponentsLoseLifeUnlessTheyDiscard(3),
                        opponentsLoseLifeUnlessTheyDiscard(3),
                        opponentsLoseLifeUnlessTheyDiscard(3),
                        opponentsLoseLifeUnlessTheyDiscard(3)),
                "\u22128: Each opponent may discard a card. If they don't, they lose 3 life. Repeat this "
                        + "process six more times."
        ));
    }

    /**
     * "Each opponent loses {@code life} life unless they discard a card": every opponent chooses in APNAP order, then
     * the chosen cards are discarded at once and the others lose the life at once (CR 101.4; Professor Onyx's ruling).
     */
    private static UnlessPaysEffect opponentsLoseLifeUnlessTheyDiscard(int life) {
        return UnlessPaysEffect.eachUnless(Subjects.eachOpponent(), new DiscardCardTypeCost(null, null),
                players -> new LoseLifeEffect(life, players)).payingTogether();
    }
}
