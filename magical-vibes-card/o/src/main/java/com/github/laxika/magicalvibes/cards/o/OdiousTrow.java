package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "125")
public class OdiousTrow extends Card {

    public OdiousTrow() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B/G}", List.of(new RegenerateEffect()), "{1}{B/G}: Regenerate this creature."));
    }
}
