package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageToOpponentEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "272")
public class SoltariGuerrillas extends Card {

    public SoltariGuerrillas() {
        // Shadow is auto-loaded from Scryfall and handled by the engine.
        // {0}: The next time this creature would deal combat damage to an opponent this turn,
        // it deals that damage to target creature instead.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new RedirectNextCombatDamageToOpponentEffect()),
                "{0}: The next time this creature would deal combat damage to an opponent this turn, "
                        + "it deals that damage to target creature instead."));
    }
}
