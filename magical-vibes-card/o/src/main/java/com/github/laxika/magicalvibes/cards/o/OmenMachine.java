package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CastCardWithoutPayingManaCostIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "148")
public class OmenMachine extends Card {

    public OmenMachine() {
        addStaticEffect(new PlayersCannotDrawCardsEffect());
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), SequenceEffect.of(
                ExileTopCardOfOwnLibraryEffect.of(Subjects.triggeringPlayer()),
                new PutCardsOntoBattlefieldEffect(
                        Subjects.cardsThisWay(StepKind.EXILE, new CardTypePredicate(CardType.LAND))),
                new CastCardWithoutPayingManaCostIfAbleEffect(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
