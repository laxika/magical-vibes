package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Maestro's Gift, the prepared spell of Inspired Skypainter (SOC 48). */
public class MaestrosGift extends Card {

    public MaestrosGift() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect(false, false))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TOKENS_CREATED_THIS_RESOLUTION));
    }
}
