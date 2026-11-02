package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "86")
public class Weftwalking extends Card {

    public Weftwalking() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new WasCast(true),
                SequenceEffect.of(
                        new ShuffleControllerHandAndGraveyardIntoLibraryEffect(),
                        new DrawCardEffect(7))));

        addStaticEffect(AlternativeCostForSpellsEffect.firstSpellOfEachPlayersTurnForFree());
    }
}
