package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "179")
public class LRienRevealed extends Card {

    public LRienRevealed() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ISLAND))),
                "Islandcycling {1} ({1}, Discard this card: Search your library for an Island card, "
                        + "reveal it, put it into your hand, then shuffle.)"
        ));
    }
}
