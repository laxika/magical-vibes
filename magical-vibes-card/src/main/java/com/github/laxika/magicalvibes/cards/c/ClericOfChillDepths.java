package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "ZNR", collectorNumber = "51")
public class ClericOfChillDepths extends Card {

    public ClericOfChillDepths() {
        // Whenever this creature blocks a creature, that creature doesn't untap
        // during its controller's next untap step.
        addEffect(EffectSlot.ON_BLOCK, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
