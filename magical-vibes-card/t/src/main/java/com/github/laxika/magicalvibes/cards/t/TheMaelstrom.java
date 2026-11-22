package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "23")
@CardRegistration(set = "OPCA", collectorNumber = "49")
public class TheMaelstrom extends Card {

    public TheMaelstrom() {
        var permanentCard = new CardIsPermanentPredicate();
        var revealPermanent = new ConditionalEffect(new CardsInLibraryAtLeast(1), new MayEffect(SequenceEffect.of(
                                new RevealTopCardOfLibraryEffect(Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, permanentCard),
                                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(permanentCard)),
                                        "Put the revealed card onto the battlefield?")),
                                PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay())),
                        "Reveal the top card of your library?"));
        addTrigger(Triggers.planeswalkToThis(), revealPermanent);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), revealPermanent);
        addTrigger(Triggers.chaosEnsues(), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(permanentCard)
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .build());
    }
}
