package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.IntuitionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "81")
@CardRegistration(set = "KHM", collectorNumber = "349")
public class BurningRuneDemon extends Card {

    public BurningRuneDemon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(SequenceEffect.of(
                IntuitionEffect.exactlyDifferentlyNamed(2,
                        new CardNotPredicate(new CardNamedPredicate("Burning-Rune Demon"))),
                new ConditionalEffect(new HappenedThisWay(StepKind.REVEAL), ChoosePlayerEffect.opponent()),
                ChooseCardEffect.oneOf(Subjects.revealedThisWay(), "a card to put into the hand")
                        .by(Subjects.playersThisWay(StepKind.CHOOSE)),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()),
                new ShuffleLibraryEffect(false)),
                "Search your library for exactly two cards with different names?"));
    }
}
