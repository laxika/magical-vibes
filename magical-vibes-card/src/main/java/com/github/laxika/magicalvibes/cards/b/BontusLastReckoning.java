package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "HOU", collectorNumber = "60")
public class BontusLastReckoning extends Card {

    public BontusLastReckoning() {
        // Destroy all creatures.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));

        // Lands you control don't untap during your next untap step.
        addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
