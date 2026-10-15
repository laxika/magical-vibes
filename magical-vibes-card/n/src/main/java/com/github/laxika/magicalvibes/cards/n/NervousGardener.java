package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "169")
public class NervousGardener extends Card {

    public NervousGardener() {
        addMorph("{G}");
        CardAnyOfPredicate basicLandTypes = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.PLAINS),
                new CardSubtypePredicate(CardSubtype.ISLAND),
                new CardSubtypePredicate(CardSubtype.SWAMP),
                new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                new CardSubtypePredicate(CardSubtype.FOREST)));
        CardAllOfPredicate landWithBasicLandType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND), basicLandTypes));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new SearchLibraryEffect(landWithBasicLandType));
    }
}
