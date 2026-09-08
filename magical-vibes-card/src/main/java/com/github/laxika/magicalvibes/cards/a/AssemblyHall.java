package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsCreatureInHandEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "286")
public class AssemblyHall extends Card {

    public AssemblyHall() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SearchLibraryForCardWithSameNameAsCreatureInHandEffect()),
                "{4}, {T}: Reveal a creature card in your hand. Search your library for a card with the same name as that card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
