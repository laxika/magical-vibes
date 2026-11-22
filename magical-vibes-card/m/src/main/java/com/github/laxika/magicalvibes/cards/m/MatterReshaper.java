package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "6")
@CardRegistration(set = "SLD", collectorNumber = "1048")
@CardRegistration(set = "EA2", collectorNumber = "2")
public class MatterReshaper extends Card {

    public MatterReshaper() {
        var match = new CardAllOfPredicate(List.of(new CardIsPermanentPredicate(), new CardMaxManaValuePredicate(3)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new RevealTopCardOfLibraryEffect(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, match),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(match)),
                        "Put the revealed card onto the battlefield?")),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay()));
    }
}
