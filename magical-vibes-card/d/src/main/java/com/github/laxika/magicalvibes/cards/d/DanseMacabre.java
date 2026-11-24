package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "22")
public class DanseMacabre extends Card {

    public DanseMacabre() {
        String label = "a creature card put into a graveyard this way";
        CardSubject sacrificedCreatureCards = Subjects.cardsThisWay(StepKind.SACRIFICE,
                new CardTypePredicate(CardType.CREATURE));
        // Each player chooses in turn, knowing the earlier choices; then all are sacrificed at once (rulings).
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()))), Subjects.eachPlayer())
                .withSimultaneousChoices());
        addSpellEffect(RollD20Effect.withAddedAmount(AmountThisWay.sacrificedToughness().controlledByYou(),
                SequenceEffect.of(
                        ChooseCardEffect.oneOf(sacrificedCreatureCards, label),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .underControlOf(Subjects.you())),
                SequenceEffect.of(
                        ChooseCardEffect.upTo(sacrificedCreatureCards, 2, label),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .underControlOf(Subjects.you()))));
    }
}
