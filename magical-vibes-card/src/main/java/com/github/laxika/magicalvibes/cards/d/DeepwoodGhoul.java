package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "127")
public class DeepwoodGhoul extends Card {

    public DeepwoodGhoul() {
        // Pay 2 life: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(2), new RegenerateEffect()),
                "Pay 2 life: Regenerate Deepwood Ghoul."));
    }
}
