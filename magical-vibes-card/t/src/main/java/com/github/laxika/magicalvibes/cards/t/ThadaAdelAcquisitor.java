package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "40")
public class ThadaAdelAcquisitor extends Card {

    public ThadaAdelAcquisitor() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new SearchTargetLibraryEffect(1, new CardTypePredicate(CardType.ARTIFACT),
                        LibrarySearchDestination.EXILE_PLAYABLE, true));
    }
}
