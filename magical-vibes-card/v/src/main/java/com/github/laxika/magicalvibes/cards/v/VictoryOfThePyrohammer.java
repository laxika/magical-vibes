package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedFromAllCreaturesDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "12")
public class VictoryOfThePyrohammer extends Card {

    public VictoryOfThePyrohammer() {
        addTrigger(Triggers.chapter(1), SequenceEffect.of(
                new MassDamageEffect(4, false, false, true, null),
                new GrantStaticEffectToSourceEffect(new DamageNotRemovedFromAllCreaturesDuringCleanupEffect())));
        addTrigger(Triggers.chapter(2), new MassDamageEffect(1, false, false, true, null));
        addTrigger(Triggers.chapter(3), new MassDamageEffect(1, false, false, true, null));
    }
}
