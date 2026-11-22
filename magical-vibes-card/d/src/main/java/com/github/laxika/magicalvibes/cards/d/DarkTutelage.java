package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "90")
@CardRegistration(set = "WOT", collectorNumber = "28")
public class DarkTutelage extends Card {

    public DarkTutelage() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.REVEAL)),
                new LoseLifeEffect(AmountThisWay.revealedManaValue(), Subjects.you())));
    }
}
