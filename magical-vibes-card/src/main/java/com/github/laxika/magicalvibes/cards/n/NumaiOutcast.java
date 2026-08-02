package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "134")
public class NumaiOutcast extends Card {

    public NumaiOutcast() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(2));
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(5), new RegenerateEffect()),
                "{B}, Pay 5 life: Regenerate this creature."));
    }
}
