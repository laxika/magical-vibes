package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "UDS", collectorNumber = "1")
public class AcademyRector extends Card {

    public AcademyRector() {
        // "When this creature dies, you may exile it. If you do, search your library for an
        // enchantment card, put that card onto the battlefield, then shuffle." The exile is the
        // resolution-time "you may" that gates the search, so both steps live inside one MayEffect.
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                SequenceEffect.of(
                        new ExileSourceCardFromGraveyardEffect(),
                        new SearchLibraryEffect(new CardTypePredicate(CardType.ENCHANTMENT),
                                LibrarySearchDestination.BATTLEFIELD)),
                "Exile Academy Rector to search your library for an enchantment?"));
    }
}
