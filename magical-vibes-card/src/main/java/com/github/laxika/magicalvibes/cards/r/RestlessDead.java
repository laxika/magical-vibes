package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "138")
public class RestlessDead extends Card {

    public RestlessDead() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()), "{B}: Regenerate Restless Dead."));
    }
}
