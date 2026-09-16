package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "SPG", collectorNumber = "71")
public class UnholyHeat extends Card {

    public UnholyHeat() {
        // Unholy Heat deals 2 damage to target creature or planeswalker.
        // Delirium — Unholy Heat deals 6 damage instead if there are four or more card types among
        // cards in your graveyard.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Delirium(),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(2),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(6)
        ));
    }
}
