package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "15")
@CardRegistration(set = "DDI", collectorNumber = "23")
@CardRegistration(set = "MD1", collectorNumber = "3")
@CardRegistration(set = "MMA", collectorNumber = "25")
@CardRegistration(set = "E02", collectorNumber = "3")
@CardRegistration(set = "MM3", collectorNumber = "17")
@CardRegistration(set = "SS2", collectorNumber = "3")
@CardRegistration(set = "GN3", collectorNumber = "15")
@CardRegistration(set = "2X2", collectorNumber = "23")
@CardRegistration(set = "2XM", collectorNumber = "25")
@CardRegistration(set = "TSR", collectorNumber = "299")
@CardRegistration(set = "MB2", collectorNumber = "15")
@CardRegistration(set = "OTP", collectorNumber = "6")
@CardRegistration(set = "MAR", collectorNumber = "4")
@CardRegistration(set = "MAR", collectorNumber = "47")
@CardRegistration(set = "SLZ", collectorNumber = "7")
@CardRegistration(set = "SLZ", collectorNumber = "128")
@CardRegistration(set = "SLZ", collectorNumber = "249")
@CardRegistration(set = "OMB", collectorNumber = "4")
@CardRegistration(set = "PZA", collectorNumber = "1")
@CardRegistration(set = "CMD", collectorNumber = "25")
public class PathToExile extends Card {

    public PathToExile() {
        // Exile target creature. Its controller may search their library for a basic land card,
        // put that card onto the battlefield tapped, then shuffle.
        // (A restricted search can always fail to find, so "may search" needs no extra flag.)
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentThenEffect(
                new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardTypePredicate(CardType.LAND)
                        )),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
