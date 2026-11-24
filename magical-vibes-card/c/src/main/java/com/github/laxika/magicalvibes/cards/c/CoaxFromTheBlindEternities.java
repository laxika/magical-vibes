package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "51")
public class CoaxFromTheBlindEternities extends Card {

    public CoaxFromTheBlindEternities() {
        addSpellEffect(SearchLibraryAndOrGraveyardForCardToHandEffect.mayTakeFrom(
                new CardSubtypePredicate(CardSubtype.ELDRAZI), Zone.OUTSIDE_GAME, Zone.EXILE));
    }
}
