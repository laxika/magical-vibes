package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "245")
public class TheStasisCoffin extends Card {

    public TheStasisCoffin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ExileSelfCost(), new GrantProtectionFromEverythingUntilNextTurnEffect()),
                "{2}, {T}, Exile The Stasis Coffin: You gain protection from everything until your next turn."
        ));
    }
}
