package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "173")
@CardRegistration(set = "EXO", collectorNumber = "79")
@CardRegistration(set = "ODY", collectorNumber = "173")
@CardRegistration(set = "TPR", collectorNumber = "125")
public class Anarchist extends Card {

    public Anarchist() {
        // When this creature enters, you may return target sorcery card from your graveyard to your hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.SORCERY))
                        .targetGraveyard(true)
                        .build(),
                "Return a sorcery card from your graveyard to your hand?"
        ));
    }
}
