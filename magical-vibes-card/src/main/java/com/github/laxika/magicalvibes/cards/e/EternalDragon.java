package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "12")
@CardRegistration(set = "VMA", collectorNumber = "26")
public class EternalDragon extends Card {

    public EternalDragon() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{3}{W}{W}: Return this card from your graveyard to your hand. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        LibrarySearchDestination.HAND)),
                "Plainscycling {2} ({2}, Discard this card: Search your library for a Plains card, reveal it, "
                        + "put it into your hand, then shuffle.)"
        ));
    }
}
