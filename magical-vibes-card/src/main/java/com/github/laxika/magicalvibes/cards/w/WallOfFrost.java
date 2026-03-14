package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;

@CardRegistration(set = "M10", collectorNumber = "80")
@CardRegistration(set = "M11", collectorNumber = "79")
public class WallOfFrost extends Card {

    public WallOfFrost() {
        // Whenever Wall of Frost blocks a creature, that creature doesn't untap
        // during its controller's next untap step.
        addEffect(EffectSlot.ON_BLOCK, new SkipNextUntapOnTargetEffect());
    }
}
