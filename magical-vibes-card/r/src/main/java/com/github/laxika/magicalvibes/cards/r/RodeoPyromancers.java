package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "143")
public class RodeoPyromancers extends Card {

    public RodeoPyromancers() {
        addTrigger(Triggers.youCastSpell(), new NthSpellCastTriggerEffect(
                1,
                List.of(new AwardManaEffect(ManaColor.RED, 2))
        ));
    }
}
