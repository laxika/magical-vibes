package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpecificCardFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "3")
public class ThoughtweftsCall extends Card {

    public ThoughtweftsCall() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Seek a Kithkin card",
                        SequenceEffect.of(
                                new SeekCardsToHandEffect(new Fixed(1), new CardSubtypePredicate(CardSubtype.KITHKIN)),
                                RegisterDelayedStepTriggerEffect.aboutSought(Triggers.endStep(ActivePlayer.ANY),
                                        new ExileSpecificCardFromHandEffect(Subjects.cardsThisWay(StepKind.SEEK)))
                                        .onlyIfStillInHand())),
                new ChooseOneEffect.ChooseOneOption(
                        "Creature cards in your hand perpetually get +1/+1",
                        new PerpetuallyBoostMatchingHandCardsEffect(
                                new CardTypePredicate(CardType.CREATURE), 1, 1))
        )));
    }
}
