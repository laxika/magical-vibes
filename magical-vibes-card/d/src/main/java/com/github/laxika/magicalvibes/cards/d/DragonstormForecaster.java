package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "43")
public class DragonstormForecaster extends Card {

    public DragonstormForecaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                        new CardNamedPredicate("Dragonstorm Globe"),
                        new CardNamedPredicate("Boulderborn Dragon")
                )))),
                "{2}, {T}: Search your library for a card named Dragonstorm Globe or Boulderborn Dragon, "
                        + "reveal it, put it into your hand, then shuffle."
        ));
    }
}
