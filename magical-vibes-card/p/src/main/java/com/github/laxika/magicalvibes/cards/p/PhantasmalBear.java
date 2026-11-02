package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "70")
@CardRegistration(set = "DDM", collectorNumber = "4")
@CardRegistration(set = "A25", collectorNumber = "69")
public class PhantasmalBear extends Card {

    public PhantasmalBear() {
        // When this creature becomes the target of a spell or ability, sacrifice it.
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());
    }
}
