package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;

@CardRegistration(set = "10E", collectorNumber = "340")
public class RazormaneMasticore extends Card {

    public RazormaneMasticore() {
        // First strike is auto-loaded from Scryfall

        // At the beginning of your upkeep, sacrifice this creature unless you discard a card.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null));

        // At the beginning of your draw step, you may have this creature deal 3 damage to target creature.
        addEffect(EffectSlot.DRAW_TRIGGERED, new MayEffect(
                new DealDamageToTargetCreatureEffect(3),
                "Deal 3 damage to target creature?"
        ));
    }
}
