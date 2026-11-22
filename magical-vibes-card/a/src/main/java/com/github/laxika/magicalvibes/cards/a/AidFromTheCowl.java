package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "105")
@CardRegistration(set = "MOC", collectorNumber = "290")
public class AidFromTheCowl extends Card {

    public AidFromTheCowl() {
        var permanentCard = new CardIsPermanentPredicate();
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentLeftBattlefieldUnderYourControlThisTurn()),
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, permanentCard),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(permanentCard)),
                        "Put the revealed card onto the battlefield?")),
                new ConditionalEffect(new AllOf(List.of(new HappenedThisWay(StepKind.REVEAL),
                        new NotCondition(new HappenedThisWay(StepKind.RETURN)))),
                        new MayEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()),
                        "Put the revealed card on the bottom of your library?")));
    }
}
