package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "148")
@CardRegistration(set = "RVR", collectorNumber = "209")
public class Petrahydrox extends Card {

    public Petrahydrox() {
        // When this creature becomes the target of a spell or ability, return it to its owner's hand.
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), ReturnToHandEffect.self());
    }
}
