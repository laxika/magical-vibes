package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "134")
public class NumaiOutcast extends Card {

    public NumaiOutcast() {
        addTrigger(Triggers.blocks(), new BushidoEffect(2));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(2));
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(5), new RegenerateEffect()),
                "{B}, Pay 5 life: Regenerate this creature."));
    }
}
