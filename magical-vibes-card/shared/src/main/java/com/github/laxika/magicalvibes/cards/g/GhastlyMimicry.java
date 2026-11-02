package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/** Back face of {@link com.github.laxika.magicalvibes.cards.m.MirrorhallMimic}. */
public class GhastlyMimicry extends Card {

    public GhastlyMimicry() {
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new CreateTokenCopyOfEnchantedPermanentEffect(List.of(CardSubtype.SPIRIT)));
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
