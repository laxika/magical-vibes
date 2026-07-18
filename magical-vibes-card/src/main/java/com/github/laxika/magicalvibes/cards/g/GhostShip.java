package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "75")
public class GhostShip extends Card {

    public GhostShip() {
        // {U}{U}{U}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{U}{U}{U}",
                List.of(new RegenerateEffect()),
                "{U}{U}{U}: Regenerate Ghost Ship."));
    }
}
