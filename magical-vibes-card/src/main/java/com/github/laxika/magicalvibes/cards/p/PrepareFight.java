package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.Fight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Prepare // Fight — front half (Prepare).
 * Instant — Untap target creature. It gets +2/+2 and gains lifelink until end of turn.
 * Back half (Fight) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "220")
public class PrepareFight extends Card {

    public PrepareFight() {
        setBackFaceCard(new Fight());

        // Untap target creature. It gets +2/+2 and gains lifelink until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }

    @Override
    public String getBackFaceClassName() {
        return "Fight";
    }
}
