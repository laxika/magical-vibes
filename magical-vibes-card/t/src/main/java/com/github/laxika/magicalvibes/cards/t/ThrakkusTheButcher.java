package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "250")
public class ThrakkusTheButcher extends Card {

    public ThrakkusTheButcher() {
        addTrigger(Triggers.attacks(), DoubleTargetCreaturePowerEffect.ofCreaturesYouControl(
                new PermanentHasSubtypePredicate(CardSubtype.DRAGON)));
    }
}
