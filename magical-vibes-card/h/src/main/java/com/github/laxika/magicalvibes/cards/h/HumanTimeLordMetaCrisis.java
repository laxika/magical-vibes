package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "585")
public class HumanTimeLordMetaCrisis extends Card {

    public HumanTimeLordMetaCrisis() {
        addTrigger(Triggers.encounterThis(),
                new EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyEffect());
    }
}
