package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost;

@CardRegistration(set = "INR", collectorNumber = "162")
public class LightningAxe extends Card {

    public LightningAxe() {
        // As an additional cost to cast this spell, discard a card or pay {5}.
        // Lightning Axe deals 5 damage to target creature.
        addEffect(EffectSlot.SPELL, new DiscardCardOrPayManaCost("{5}"));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
    }
}
