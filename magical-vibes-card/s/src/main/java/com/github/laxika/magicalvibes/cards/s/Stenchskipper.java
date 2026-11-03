package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "79")
public class Stenchskipper extends Card {

    public Stenchskipper() {
        // At the beginning of the end step, if you control no Goblins, sacrifice this creature. The
        // count includes Stenchskipper itself, so a Goblin Stenchskipper is never sacrificed.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)))),
                new SacrificeSelfEffect());
    }
}
