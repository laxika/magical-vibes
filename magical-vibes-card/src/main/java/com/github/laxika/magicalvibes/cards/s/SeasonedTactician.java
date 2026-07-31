package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "17")
public class SeasonedTactician extends Card {

    public SeasonedTactician() {
        // {3}, Exile the top four cards of your library: The next time a source of your choice
        // would deal damage to you this turn, prevent that damage.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new ExileTopCardOfLibraryCost(4),
                        PreventDamageFromChosenSourceEffect.nextDamageToYou()
                ),
                "{3}, Exile the top four cards of your library: The next time a source of your choice "
                        + "would deal damage to you this turn, prevent that damage."
        ));
    }
}
