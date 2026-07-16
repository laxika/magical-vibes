package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "DRB", collectorNumber = "8")
public class HellkiteOverlord extends Card {

    public HellkiteOverlord() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));

        addActivatedAbility(new ActivatedAbility(false, "{B}{G}", List.of(new RegenerateEffect()),
                "{B}{G}: Regenerate this creature."));
    }
}
