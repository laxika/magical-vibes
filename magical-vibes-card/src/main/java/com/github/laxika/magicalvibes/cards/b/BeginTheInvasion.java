package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MOC", collectorNumber = "79")
@CardRegistration(set = "MOC", collectorNumber = "87")
public class BeginTheInvasion extends Card {

    public BeginTheInvasion() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new XValue(),
                new CardTypePredicate(CardType.BATTLE),
                LibrarySearchDestination.BATTLEFIELD,
                null,
                true));
    }
}
