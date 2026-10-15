package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfCreatureEffect;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "5")
public class BeaconOfDestiny extends Card {

    public BeaconOfDestiny() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new RedirectNextDamageFromChosenSourceToSelfCreatureEffect()),
                "{T}: The next time a source of your choice would deal damage to you this turn, "
                        + "that damage is dealt to this creature instead."));
    }
}
