package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "200")
public class LavaRunner extends Card {

    public LavaRunner() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                        Subjects.controllerOf(Subjects.triggeringSpell())));
    }
}
