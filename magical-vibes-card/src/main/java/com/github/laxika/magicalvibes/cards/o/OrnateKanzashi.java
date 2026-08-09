package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect;
import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "157")
public class OrnateKanzashi extends Card {

    public OrnateKanzashi() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect()),
                "{2}, {T}: Target opponent exiles the top card of their library. You may play that card this turn."
        ));
    }
}
