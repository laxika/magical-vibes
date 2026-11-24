package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithCardsPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "104")
public class CreepingDread extends Card {

    public CreepingDread() {
        // At the beginning of your upkeep, each player discards a card. Each opponent who discarded
        // a card that shares a card type with the card you discarded loses 3 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new DiscardEffect(1, Subjects.eachPlayer()),
                new LoseLifeEffect(3, Subjects.except(
                        Subjects.playersThisWay(StepKind.DISCARD, new CardSharesCardTypeWithCardsPredicate(
                                Subjects.ownedBy(Subjects.cardsThisWay(StepKind.DISCARD), Subjects.you()))),
                        Subjects.you()))));
    }
}
