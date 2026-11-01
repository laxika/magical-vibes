package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "76")
public class InfernalCaretaker extends Card {

    public InfernalCaretaker() {
        addMorph("{3}{B}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardSubtypePredicate(CardSubtype.ZOMBIE))
                        .returnAll(true)
                        .build());
    }
}
