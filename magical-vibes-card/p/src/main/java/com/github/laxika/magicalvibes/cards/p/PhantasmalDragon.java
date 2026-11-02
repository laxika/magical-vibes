package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "71")
@CardRegistration(set = "DDM", collectorNumber = "14")
public class PhantasmalDragon extends Card {

    public PhantasmalDragon() {
        // When this creature becomes the target of a spell or ability, sacrifice it.
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());
    }
}
