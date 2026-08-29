package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "RNA", collectorNumber = "149")
public class WildernessReclamation extends Card {

    public WildernessReclamation() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
