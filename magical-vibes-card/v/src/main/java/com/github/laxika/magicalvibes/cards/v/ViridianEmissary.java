package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "95")
@CardRegistration(set = "PCA", collectorNumber = "80")
@CardRegistration(set = "PC2", collectorNumber = "80")
@CardRegistration(set = "TD2", collectorNumber = "52")
@CardRegistration(set = "C15", collectorNumber = "208")
@CardRegistration(set = "CMA", collectorNumber = "162")
public class ViridianEmissary extends Card {

    public ViridianEmissary() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(new CardSupertypePredicate(CardSupertype.BASIC), new CardTypePredicate(CardType.LAND))),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "Search your library for a basic land card, put it onto the battlefield tapped?"));
    }
}
