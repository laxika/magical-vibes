package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "8")
public class ClaimJumper extends Card {

    public ClaimJumper() {
        SearchLibraryEffect searchPlains = new SearchLibraryEffect(
                new Fixed(1), new CardSubtypePredicate(CardSubtype.PLAINS),
                LibrarySearchDestination.BATTLEFIELD_TAPPED, null, 1, false,
                false, false, false, null, LibrarySearchPlayer.CONTROLLER, false, false, false);
        ConditionalEffect moreLands = new ConditionalEffect(new OpponentControlsMoreLands(),
                new MayEffect(searchPlains, "Search your library for another Plains card?"));
        ConditionalEffect moreLandsAfterDecline = new ConditionalEffect(new OpponentControlsMoreLands(),
                new MayEffect(
                        SequenceEffect.of(searchPlains, new ShuffleLibraryEffect(false)),
                        "Search your library for another Plains card?"));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new OpponentControlsMoreLands(), new MayEffect(
                        SequenceEffect.of(searchPlains, moreLands, new ShuffleLibraryEffect(false)),
                        "Search your library for a Plains card?",
                        moreLandsAfterDecline)));
    }
}
