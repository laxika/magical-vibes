package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "311")
public class PlanarPortal extends Card {

    public PlanarPortal() {
        addActivatedAbility(new ActivatedAbility(true, "{6}", List.of(new SearchLibraryEffect()),
                "{6}, {T}: Search your library for a card, put that card into your hand, then shuffle."));
    }
}
