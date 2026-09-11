package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "NEO", collectorNumber = "220")
public class GreasefangOkibaBoss extends Card {

    public GreasefangOkibaBoss() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardSubtypePredicate(CardSubtype.VEHICLE))
                        .targetGraveyard(true)
                        .grantHaste(true)
                        .returnToHandAtEndStep(true)
                        .build());
    }
}
