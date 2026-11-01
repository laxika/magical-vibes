package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "175")
@CardRegistration(set = "KLR", collectorNumber = "267")
@CardRegistration(set = "BRR", collectorNumber = "49")
@CardRegistration(set = "MOC", collectorNumber = "373")
@CardRegistration(set = "C21", collectorNumber = "260")
public class ScrapTrawler extends Card {

    public ScrapTrawler() {
        ReturnCardFromGraveyardEffect returnArtifact = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.ARTIFACT))
                .targetGraveyard(true)
                .dynamicMaxManaValue(new SourceManaValueMinusOne())
                .build();
        addTrigger(Triggers.dies(ObjectRelation.THIS), returnArtifact);
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                TriggeringArtifactControllerConditionalEffect.another(returnArtifact));
    }
}
