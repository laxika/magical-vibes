package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/** Back face of {@link com.github.laxika.magicalvibes.cards.m.MirrorhallMimic}. */
public class GhastlyMimicry extends Card {

    public GhastlyMimicry() {
        target(TargetFilters.creature()).addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new CreateTokenCopyOfEnchantedPermanentEffect(List.of(CardSubtype.SPIRIT)));
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
