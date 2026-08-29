package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "127")
public class SkeletalWurm extends Card {

    public SkeletalWurm() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature."));
    }
}
