package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "133")
public class InnocentBystander extends Card {

    public InnocentBystander() {
        addTrigger(Triggers.dealtDamage(),
                new ConditionalEffect(new EventValueAtLeast(3), CreateTokenEffect.ofClueToken(1)));
    }
}
