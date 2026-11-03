package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "126")
public class TenuredOilcaster extends Card {

    public TenuredOilcaster() {
        addStaticEffect(new ConditionalEffect(
                new OpponentGraveyardAtLeast(8),
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), new MillEffect(1, MillRecipient.CONTROLLER));
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
