package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "70")
public class TetheredSkirge extends Card {

    public TetheredSkirge() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new LoseLifeEffect(1));
    }
}
