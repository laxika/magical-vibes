package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MB2", collectorNumber = "366")
@CardRegistration(set = "MB2", collectorNumber = "605")
public class WrathOfLeknif extends Card {

    public WrathOfLeknif() {
        // Destroy all creatures. They can't be regenerated.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(), true));
        // Untap up to four lands you control.
        addEffect(EffectSlot.SPELL,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate(), 4));
    }
}
