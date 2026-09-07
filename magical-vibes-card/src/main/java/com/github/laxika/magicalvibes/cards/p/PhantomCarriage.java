package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasDisturbPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "70")
public class PhantomCarriage extends Card {

    public PhantomCarriage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(
                        new Fixed(1),
                        new CardAnyOfPredicate(List.of(new CardHasFlashbackPredicate(), new CardHasDisturbPredicate())),
                        LibrarySearchDestination.GRAVEYARD),
                        "Search your library for a card with flashback or disturb?"));
    }
}
