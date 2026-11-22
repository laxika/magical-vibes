package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEventTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "139")
public class TheRuinousPowers extends Card {

    private static final CardTypePredicate LAND = new CardTypePredicate(CardType.LAND);

    public TheRuinousPowers() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new ChoosePlayerAtRandomEffect(Subjects.eachOpponent()),
                        new ExileTopCardOfOwnLibraryEffect(
                                false, Subjects.playersThisWay(StepKind.CHOOSE), new Fixed(1), true),
                        // "To cast it": a land is played, not cast.
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE, LAND)),
                        AllowPlayCardsFromExileEffect.thisTurn(
                                        Subjects.cardsThisWay(StepKind.EXILE, new CardNotPredicate(LAND)))
                                .withAnyManaType(),
                        RegisterDelayedEventTriggerEffect.whenYouCastExiledCard(
                                new LoseLifeEffect(new TargetSpellManaValue(),
                                        Subjects.ownerOf(Subjects.triggeringCard())))));
    }
}
