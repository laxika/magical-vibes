package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMoreCreaturesThanTargetSpellController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "ROE", collectorNumber = "92")
public class UnifiedWill extends Card {

    public UnifiedWill() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControllerControlsMoreCreaturesThanTargetSpellController(),
                new CounterSpellEffect()));
    }
}
