package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Bruna, Light of Alabaster — "Whenever Bruna attacks or blocks, you may attach to it any number of
 * Auras on the battlefield and you may put onto the battlefield attached to it any number of Aura
 * cards that could enchant it from your graveyard and/or hand."
 */
@CardRegistration(set = "AVR", collectorNumber = "208")
@CardRegistration(set = "SLD", collectorNumber = "1221")
@CardRegistration(set = "C18", collectorNumber = "170")
public class BrunaLightOfAlabaster extends Card {

    public BrunaLightOfAlabaster() {
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE,
                new AttachAurasToSourceEffect());
    }
}
