package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsExileRandomRestOnTopEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "209")
public class OrcishLibrarian extends Card {

    public OrcishLibrarian() {
        // {R}, {T}: Look at the top eight cards of your library. Exile four of them at random,
        // then put the rest on top of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new LookAtTopCardsExileRandomRestOnTopEffect(8, 4)),
                "{R}, {T}: Look at the top eight cards of your library. Exile four of them at random, then put the rest on top of your library in any order."
        ));
    }
}
