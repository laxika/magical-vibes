package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomMatchingCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "18")
public class BankJob extends Card {

    public BankJob() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new ExileBottomMatchingCardOfLibraryEffect(new CardTypePredicate(CardType.CREATURE)),
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE)),
                        RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                                        new PutExiledCardsIntoOwnersGraveyardEffect(
                                                Subjects.cardsThisWay(StepKind.EXILE)),
                                        CreateTokenEffect.ofTreasureToken(1))
                                .onlyIfAnyStillExiled()));
    }
}
