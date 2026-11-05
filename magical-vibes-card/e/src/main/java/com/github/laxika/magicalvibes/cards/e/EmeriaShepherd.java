package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "22")
@CardRegistration(set = "FDC", collectorNumber = "27")
@CardRegistration(set = "ZNC", collectorNumber = "16")
public class EmeriaShepherd extends Card {

    public EmeriaShepherd() {
        CardPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        PermanentHasSubtypePredicate plains = new PermanentHasSubtypePredicate(CardSubtype.PLAINS);
        ReturnCardFromGraveyardEffect returnToHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(nonlandPermanent)
                .targetGraveyard(true)
                .build();
        ReturnCardFromGraveyardEffect returnToBattlefield = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(nonlandPermanent)
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .build();
        MayEffect mayReturnToHand = new MayEffect(returnToHand, "Return that card to your hand?");

        // Landfall - Whenever a land you control enters, you may return target nonland permanent card
        // from your graveyard to your hand. If that land is a Plains, you may return that nonland
        // permanent card to the battlefield instead. The land is checked as it entered: each branch is the
        // trigger's filter on it.
        target(new GraveyardCardPredicateTargetFilter(
                nonlandPermanent, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.LAND, plains)),
                                ObjectRelation.YOU_CONTROL),
                        new MayEffect(returnToBattlefield,
                                "Return that card to the battlefield instead?", mayReturnToHand))
                .addTrigger(Triggers.enters(new PermanentAllOfPredicate(List.of(Triggers.LAND,
                                new PermanentNotPredicate(plains))), ObjectRelation.YOU_CONTROL),
                        mayReturnToHand);
    }
}
