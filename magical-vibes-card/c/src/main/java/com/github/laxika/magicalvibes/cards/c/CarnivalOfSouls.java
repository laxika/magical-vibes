package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "UDS", collectorNumber = "55")
public class CarnivalOfSouls extends Card {

    public CarnivalOfSouls() {
        // Whenever a creature enters, you lose 1 life and add {B} — including this permanent when it
        // enters as a creature (Opalescence).
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                SequenceEffect.of(new LoseLifeEffect(1), new AwardManaEffect(ManaColor.BLACK)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new SourceIsCreature(),
                SequenceEffect.of(new LoseLifeEffect(1), new AwardManaEffect(ManaColor.BLACK))));
    }
}
