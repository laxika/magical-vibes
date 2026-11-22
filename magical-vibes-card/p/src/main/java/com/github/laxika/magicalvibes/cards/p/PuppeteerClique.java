package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "75")
@CardRegistration(set = "MM2", collectorNumber = "91")
@CardRegistration(set = "ECC", collectorNumber = "84")
@CardRegistration(set = "NCC", collectorNumber = "257")
@CardRegistration(set = "WOC", collectorNumber = "115")
@CardRegistration(set = "C17", collectorNumber = "121")
public class PuppeteerClique extends Card {

    public PuppeteerClique() {
        // Flying and Persist are auto-loaded from Scryfall (Persist is handled by the engine).
        // When this creature enters, put target creature card from an opponent's graveyard onto
        // the battlefield under your control. It gains haste. At the beginning of your next end
        // step, exile it.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                        .targetGraveyard(true)
                        .grantHaste(true)
                        .exileAtYourNextEndStep(true)
                        .build());
    }
}
