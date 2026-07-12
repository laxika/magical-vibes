package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "81")
public class GreenSunsZenith extends Card {

    public GreenSunsZenith() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(new CardColorPredicate(CardColor.GREEN), new CardTypePredicate(CardType.CREATURE))),
                LibrarySearchDestination.BATTLEFIELD, new ManaValueBound(false, 0)));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
