package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromSelfToYouEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "44")
public class Mercenaries extends Card {

    public Mercenaries() {
        // {3}: The next time this creature would deal damage to you this turn, prevent that damage.
        // Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PreventNextDamageFromSelfToYouEffect()),
                "{3}: The next time this creature would deal damage to you this turn, prevent that damage. "
                        + "Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
