package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "205")
public class WolfirAvenger extends Card {

    public WolfirAvenger() {
        // {1}{G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new RegenerateEffect()),
                "{1}{G}: Regenerate Wolfir Avenger."));
    }
}
