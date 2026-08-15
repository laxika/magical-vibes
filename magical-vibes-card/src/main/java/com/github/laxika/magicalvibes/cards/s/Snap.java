package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "43")
public class Snap extends Card {

    public Snap() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(
                TapUntapScope.ALL_PERMANENTS, new PermanentIsLandPredicate(), 2));
    }
}
