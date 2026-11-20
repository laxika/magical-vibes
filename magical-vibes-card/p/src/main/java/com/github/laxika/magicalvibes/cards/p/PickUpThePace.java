package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "702")
public class PickUpThePace extends Card {

    public PickUpThePace() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentEnteredBattlefieldThisTurnPredicate()),
                SequenceEffect.of(
                        ExileTopCardOfOwnLibraryEffect.linkedToSource(Subjects.you()),
                        AllowPlayCardsFromExileEffect.whileYouControlSource(Subjects.cardsThisWay(StepKind.EXILE))
                                .playableOnlyIf(new AttackedWithCreaturesThisTurn(1))));
    }
}
