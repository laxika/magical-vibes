package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "55")
public class CarnivalOfSouls extends Card {

    public CarnivalOfSouls() {
        // Whenever a creature enters, you lose 1 life and add {B} — including this permanent when it
        // enters as a creature (Opalescence).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                SequenceEffect.of(new LoseLifeEffect(1), new AwardManaEffect(ManaColor.BLACK)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(new SourceIsCreature(),
                SequenceEffect.of(new LoseLifeEffect(1), new AwardManaEffect(ManaColor.BLACK))));
    }
}
