package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardsOfLibraryOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "1")
public class DeceiverOfForm extends Card {

    public DeceiverOfForm() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new ConditionalEffect(
                        new TopCardOfLibraryType(CardType.CREATURE),
                        new MayEffect(
                                BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.REVEAL,
                                                new CardTypePredicate(CardType.CREATURE)))
                                        .by(Subjects.except(Subjects.controlledBy(Subjects.you(),
                                                new PermanentIsCreaturePredicate()), Subjects.source()))
                                        .untilEndOfTurn(),
                                "Have other creatures you control become copies of that card until end of turn?")),
                new ConditionalEffect(
                        new CardsInLibraryAtLeast(1),
                        new MayEffect(
                                new PutTopCardsOfLibraryOnBottomEffect(1),
                                "Put that card on the bottom of your library?"))));
    }
}
