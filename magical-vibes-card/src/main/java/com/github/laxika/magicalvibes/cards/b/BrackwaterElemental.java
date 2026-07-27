package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;


@CardRegistration(set = "CON", collectorNumber = "21")
public class BrackwaterElemental extends Card {

    public BrackwaterElemental() {
        // When this creature attacks or blocks, sacrifice it at the beginning of the next end step.
        addEffect(EffectSlot.ON_ATTACK, new SacrificeSelfAtEndStepEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeSelfAtEndStepEffect());

        // Unearth {2}{U}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step or if it would leave the battlefield.
        // Unearth only as a sorcery.
        addUnearth("{2}{U}");
    }
}
