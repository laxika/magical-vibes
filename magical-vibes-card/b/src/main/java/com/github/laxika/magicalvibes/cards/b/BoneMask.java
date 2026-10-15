package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "295")
public class BoneMask extends Card {

    public BoneMask() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(PreventDamageFromChosenSourceEffect.nextDamageToYouAndExileFromLibrary()),
                "The next time a source of your choice would deal damage to you this turn, prevent that damage. "
                        + "Exile cards from the top of your library equal to the damage prevented this way."
        ));
    }
}
