package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "214")
public class ApesOfRath extends Card {

    public ApesOfRath() {
        // Whenever this creature attacks, it doesn't untap during its controller's next untap step.
        addTrigger(Triggers.attacks(), new SkipNextUntapEffect(TapUntapScope.SELF));
    }
}
