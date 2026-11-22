package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "82")
public class CausticBronco extends Card {

    public CausticBronco() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.REVEAL)),
                new ConditionalReplacementEffect(
                        new SourceIsSaddled(),
                        new LoseLifeEffect(AmountThisWay.revealedManaValue(), Subjects.you()),
                        new LoseLifeEffect(AmountThisWay.revealedManaValue(), Subjects.eachOpponent()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(3), new BecomeSaddledUntilEndOfTurnEffect(Subjects.source())),
                "Saddle 3",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
