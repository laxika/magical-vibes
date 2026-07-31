package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALL", collectorNumber = "44a")
@CardRegistration(set = "ALL", collectorNumber = "44b")
public class CastingOfBones extends Card {

    public CastingOfBones() {
        // Enchant creature
        target(TargetFilters.creature())
        // When enchanted creature dies, draw three cards, then discard one of them.
        .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                SequenceEffect.of(new DrawCardEffect(3), new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
