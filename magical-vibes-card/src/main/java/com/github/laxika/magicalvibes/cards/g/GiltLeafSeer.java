package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "215")
public class GiltLeafSeer extends Card {

    public GiltLeafSeer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new ReorderTopCardsOfLibraryEffect(2)),
                "{G}, {T}: Look at the top two cards of your library, then put them back in any order."
        ));
    }
}
