package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "257")
public class HornedTroll extends Card {

    public HornedTroll() {
        // {G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate Horned Troll."));
    }
}
