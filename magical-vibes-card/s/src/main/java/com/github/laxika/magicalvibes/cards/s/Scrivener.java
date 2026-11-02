package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "46")
@CardRegistration(set = "ODY", collectorNumber = "100")
@CardRegistration(set = "TPR", collectorNumber = "64")
@CardRegistration(set = "VMA", collectorNumber = "91")
public class Scrivener extends Card {

    public Scrivener() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.INSTANT))
                .targetGraveyard(true)
                .build(), "Return target instant card to your hand?"));
    }
}
