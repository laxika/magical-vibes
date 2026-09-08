package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "TDM", collectorNumber = "204")
public class LotuslightDancers extends Card {

    public LotuslightDancers() {
        // Search for one black card, one green card, and one blue card, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, searchFor(CardColor.BLACK));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, searchFor(CardColor.GREEN));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, searchFor(CardColor.BLUE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ShuffleLibraryEffect(false));
    }

    private static SearchLibraryEffect searchFor(CardColor color) {
        return new SearchLibraryEffect(
                new Fixed(1),
                new CardColorPredicate(color),
                LibrarySearchDestination.GRAVEYARD,
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
