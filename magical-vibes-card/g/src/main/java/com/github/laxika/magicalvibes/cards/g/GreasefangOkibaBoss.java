package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "220")
@CardRegistration(set = "SLD", collectorNumber = "1690")
public class GreasefangOkibaBoss extends Card {

    public GreasefangOkibaBoss() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardSubtypePredicate(CardSubtype.VEHICLE))
                        .targetGraveyard(true)
                        .grantHaste(true)
                        .returnToHandAtYourNextEndStep(true)
                        .build());
    }
}
