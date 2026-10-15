package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "269")
public class JungleTroll extends Card {

    public JungleTroll() {
        // {R}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new RegenerateEffect()),
                "{R}: Regenerate Jungle Troll."));

        // {G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate Jungle Troll."));
    }
}
