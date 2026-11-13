package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "99b")
public class RagnarokDivineDeliverance extends Card {

    public RagnarokDivineDeliverance() {
        // One ability of two targets, each its own group: the permanent destroyed and the card returned.
        CardPredicate nonlegendaryPermanentCard = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY))));
        CardEffect destroy = new DestroyTargetPermanentEffect();
        CardEffect returnCard = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(nonlegendaryPermanentCard)
                .targetGraveyard(true)
                .build();
        addTrigger(Triggers.dies(ObjectRelation.THIS), destroy, returnCard);
        target(TargetFilters.permanent()).bindEffect(destroy);
        target(new GraveyardCardPredicateTargetFilter(nonlegendaryPermanentCard,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD)).bindEffect(returnCard);
    }
}
