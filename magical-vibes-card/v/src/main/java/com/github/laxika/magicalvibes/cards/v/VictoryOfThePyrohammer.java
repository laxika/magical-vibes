package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "12")
public class VictoryOfThePyrohammer extends Card {

    public VictoryOfThePyrohammer() {
        addTrigger(Triggers.chapter(1), SequenceEffect.of(
                DealDamageEffect.toEachCreatureAndPlaneswalker(4),
                new GrantStaticEffectToSourceEffect(
                        new DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect(Subjects.allCreatures()))));
        addTrigger(Triggers.chapter(2), DealDamageEffect.toEachCreatureAndPlaneswalker(1));
        addTrigger(Triggers.chapter(3), DealDamageEffect.toEachCreatureAndPlaneswalker(1));
    }
}
