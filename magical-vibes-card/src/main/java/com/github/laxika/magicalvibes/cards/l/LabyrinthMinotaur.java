package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "5ED", collectorNumber = "97")
public class LabyrinthMinotaur extends Card {

    public LabyrinthMinotaur() {
        // Whenever this creature blocks a creature, that creature doesn't untap
        // during its controller's next untap step.
        addEffect(EffectSlot.ON_BLOCK, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
