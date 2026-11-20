package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;

@CardRegistration(set = "USG", collectorNumber = "130")
@CardRegistration(set = "PD3", collectorNumber = "22")
@CardRegistration(set = "TD2", collectorNumber = "75")
@CardRegistration(set = "C15", collectorNumber = "120")
@CardRegistration(set = "CMA", collectorNumber = "53")
public class DiabolicServitude extends Card {

    public DiabolicServitude() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .linkToSource(true)
                .build());
        addTrigger(Triggers.creatureDies(new PermanentIsChosenPermanentPredicate(), ObjectRelation.ANY),
                SequenceEffect.of(
                        new ExileTriggeringCardFromGraveyardEffect(),
                        ReturnToHandEffect.self()));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.EXILE));
    }
}
