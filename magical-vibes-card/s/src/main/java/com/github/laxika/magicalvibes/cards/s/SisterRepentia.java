package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "142")
public class SisterRepentia extends Card {

    public SisterRepentia() {
        // Martyrdom — When this creature dies, you gain 2 life and draw two cards.
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new GainLifeEffect(2),
                new DrawCardEffect(2)));

        // Miracle {W}{B}
        addCastingOption(new MiracleCast("{W}{B}"));
    }
}
