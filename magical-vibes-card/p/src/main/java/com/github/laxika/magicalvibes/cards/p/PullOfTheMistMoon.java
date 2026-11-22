package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "3")
public class PullOfTheMistMoon extends Card {

    public PullOfTheMistMoon() {
        addStaticEffect(new KickerEffect("{1}{U}"));

        // When Pull of the Mist Moon enters, exile target nonland permanent an opponent controls
        // until Pull of the Mist Moon leaves the battlefield.
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), exileUntilItLeaves());

        // If it was kicked, choose a nonland permanent card in your hand. It perpetually gains the
        // same exile ability when it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                ChooseCardEffect.announced(
                        Subjects.cardsInYourHand(new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))))),
                        "a nonland permanent card in your hand"),
                new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                        Triggers.enters(ObjectRelation.THIS), exileUntilItLeaves()));
    }

    private static CardEffect exileUntilItLeaves() {
        return new ExileTargetPermanentUntilSourceLeavesEffect(
                false, TargetFilters.nonlandPermanentAnOpponentControls().predicate());
    }
}
