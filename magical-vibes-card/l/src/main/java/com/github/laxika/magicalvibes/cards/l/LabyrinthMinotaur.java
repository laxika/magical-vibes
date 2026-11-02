package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "97")
@CardRegistration(set = "HML", collectorNumber = "30a")
@CardRegistration(set = "HML", collectorNumber = "30b")
@CardRegistration(set = "ME3", collectorNumber = "39")
public class LabyrinthMinotaur extends Card {

    public LabyrinthMinotaur() {
        // Whenever this creature blocks a creature, that creature doesn't untap
        // during its controller's next untap step.
        addTrigger(Triggers.blocks(), new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
