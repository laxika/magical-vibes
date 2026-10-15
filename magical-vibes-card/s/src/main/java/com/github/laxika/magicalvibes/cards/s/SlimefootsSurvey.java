package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "178")
public class SlimefootsSurvey extends Card {

    public SlimefootsSurvey() {
        CardAnyOfPredicate basicLandType = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.PLAINS),
                new CardSubtypePredicate(CardSubtype.ISLAND),
                new CardSubtypePredicate(CardSubtype.SWAMP),
                new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                new CardSubtypePredicate(CardSubtype.FOREST)
        ));
        CardAllOfPredicate landWithBasicLandType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                basicLandType
        ));

        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new Fixed(2), landWithBasicLandType,
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new BasicLandTypesAmongControlledLands(), new Fixed(1), null,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.TOP_OF_LIBRARY, true));
    }
}
