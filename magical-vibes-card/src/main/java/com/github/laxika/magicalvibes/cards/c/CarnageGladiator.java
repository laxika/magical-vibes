package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "61")
public class CarnageGladiator extends Card {

    public CarnageGladiator() {
        // The blocking creature is baked onto the trigger as its non-targeting target, so
        // TARGET_PERMANENT_CONTROLLER is "that creature's controller".
        addEffect(EffectSlot.ON_ANY_CREATURE_BLOCKS,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{R}",
                List.of(new RegenerateEffect()),
                "{1}{B}{R}: Regenerate Carnage Gladiator."
        ));
    }
}
