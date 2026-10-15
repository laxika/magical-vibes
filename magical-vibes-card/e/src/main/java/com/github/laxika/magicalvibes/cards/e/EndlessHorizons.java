package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "EVE", collectorNumber = "4")
public class EndlessHorizons extends Card {

    public EndlessHorizons() {
        // When this enchantment enters, search your library for any number of Plains cards, exile them, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForCardsToExileWithSourceEffect(new CardSubtypePredicate(CardSubtype.PLAINS)));
        // At the beginning of your upkeep, you may put a card you own exiled with this enchantment into your hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new PutCardExiledWithSourceIntoHandEffect(),
                        "Put a card exiled with Endless Horizons into your hand?"));
    }
}
