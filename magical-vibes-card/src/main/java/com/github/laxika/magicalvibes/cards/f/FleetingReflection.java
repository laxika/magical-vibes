package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "49")
public class FleetingReflection extends Card {

    public FleetingReflection() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL,
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL,
                        new MakeTargetCopyOfTargetCreatureUntilEndOfTurnEffect());
        target(TargetFilters.creature(), 0, 1);
    }
}
