package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowTargetPlayerToTargetSourceIgnoringShroudEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "83")
public class AutumnWillow extends Card {

    public AutumnWillow() {
        // Shroud is auto-loaded from Scryfall.

        // {G}: Until end of turn, Autumn Willow can be the target of spells and abilities controlled
        // by target player as though it didn't have shroud.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new AllowTargetPlayerToTargetSourceIgnoringShroudEffect()),
                "{G}: Until end of turn, Autumn Willow can be the target of spells and abilities "
                        + "controlled by target player as though it didn't have shroud."
        ));
    }
}
