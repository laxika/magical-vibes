package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class OKagachiMadeManifest extends Card {

    public OKagachiMadeManifest() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                ChooseCardEffect.oneOf(Subjects.cardsInYourGraveyard(
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                "a nonland card in your opponent's graveyard")
                        .by(Subjects.defendingPlayer()),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                new ConditionalEffect(new HappenedThisWay(StepKind.CHOOSE),
                        new BoostSelfEffect(AmountThisWay.chosenManaValue(), new Fixed(0)))));
    }
}
