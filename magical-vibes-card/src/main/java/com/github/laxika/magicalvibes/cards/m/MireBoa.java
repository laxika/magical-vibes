package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "135")
public class MireBoa extends Card {

    public MireBoa() {
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new RegenerateEffect()),
                "{G}: Regenerate this creature."));
    }
}
