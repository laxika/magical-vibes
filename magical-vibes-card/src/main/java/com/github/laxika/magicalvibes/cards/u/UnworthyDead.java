package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "163")
public class UnworthyDead extends Card {

    public UnworthyDead() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature."));
    }
}
