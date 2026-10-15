package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "77")
public class GaeasBalance extends Card {

    public GaeasBalance() {
        addEffect(EffectSlot.SPELL, new SacrificeMultiplePermanentsCost(5, new PermanentIsLandPredicate()));
        addEffect(EffectSlot.SPELL, search(CardSubtype.PLAINS));
        addEffect(EffectSlot.SPELL, search(CardSubtype.ISLAND));
        addEffect(EffectSlot.SPELL, search(CardSubtype.SWAMP));
        addEffect(EffectSlot.SPELL, search(CardSubtype.MOUNTAIN));
        addEffect(EffectSlot.SPELL, search(CardSubtype.FOREST));
        addEffect(EffectSlot.SPELL, new ShuffleLibraryEffect(false));
    }

    private static SearchLibraryEffect search(CardSubtype subtype) {
        return new SearchLibraryEffect(
                new Fixed(1),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardSubtypePredicate(subtype))),
                LibrarySearchDestination.BATTLEFIELD,
                null,
                1,
                false,
                false,
                false,
                false,
                null,
                LibrarySearchPlayer.CONTROLLER,
                false,
                false,
                false);
    }
}
