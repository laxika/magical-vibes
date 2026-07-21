package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HOU", collectorNumber = "120")
public class HourOfPromise extends Card {

    public HourOfPromise() {
        // Search your library for up to two land cards, put them onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(2),
                new CardTypePredicate(CardType.LAND),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        // Then if you control three or more Deserts, create two 2/2 black Zombie creature tokens.
        // Resolved after the search so fetched Deserts count.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentHasSubtypePredicate(CardSubtype.DESERT)),
                CreateTokenEffect.blackZombie(2)));
    }
}
