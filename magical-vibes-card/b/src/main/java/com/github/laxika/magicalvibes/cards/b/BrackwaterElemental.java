package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "CON", collectorNumber = "21")
@CardRegistration(set = "DDH", collectorNumber = "46")
public class BrackwaterElemental extends Card {

    public BrackwaterElemental() {
        // When this creature attacks or blocks, sacrifice it at the beginning of the next end step.
        addTrigger(Triggers.attacks(), new SacrificeSelfAtEndStepEffect());
        addTrigger(Triggers.blocks(), new SacrificeSelfAtEndStepEffect());

        // Unearth {2}{U}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step or if it would leave the battlefield.
        // Unearth only as a sorcery.
        addUnearth("{2}{U}");
    }
}
