package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AbilityTargetChoiceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "MSH", collectorNumber = "65")
public class LokiGodOfMischief extends Card {

    public LokiGodOfMischief() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CHOOSES_TARGETS,
                new AbilityTargetChoiceTriggerEffect(new OncePerTurnTriggerEffect(new DrawCardEffect())));
    }
}
