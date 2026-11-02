package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "2")
@CardRegistration(set = "TPR", collectorNumber = "2")
@CardRegistration(set = "DDC", collectorNumber = "6")
@CardRegistration(set = "DVD", collectorNumber = "6")
public class AngelicProtector extends Card {

    public AngelicProtector() {
        // Whenever this creature becomes the target of a spell or ability,
        // this creature gets +0/+3 until end of turn.
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new BoostSelfEffect(0, 3));
    }
}
