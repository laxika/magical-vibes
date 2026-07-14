package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "41")
public class RavensCrime extends Card {

    public RavensCrime() {
        // Target player discards a card.
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
        // Retrace — may be recast from the graveyard by additionally discarding a land card.
        addCastingOption(new Retrace());
    }
}
