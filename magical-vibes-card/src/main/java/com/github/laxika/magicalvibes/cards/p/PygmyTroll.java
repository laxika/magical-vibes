package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "118")
public class PygmyTroll extends Card {

    public PygmyTroll() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new RegenerateEffect()),
                "{G}: Regenerate this creature."));
    }
}
