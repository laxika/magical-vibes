package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "CSP", collectorNumber = "135")
public class ZurTheEnchanter extends Card {

    public ZurTheEnchanter() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SearchLibraryEffect(
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        LibrarySearchDestination.BATTLEFIELD,
                        new ManaValueBound(new Fixed(3), false, 0)),
                "Search your library for an enchantment card with mana value 3 or less?"));
    }
}
